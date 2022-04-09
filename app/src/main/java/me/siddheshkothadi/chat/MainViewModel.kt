package me.siddheshkothadi.chat

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.siddheshkothadi.chat.model.Message
import me.siddheshkothadi.chat.model.User
import me.siddheshkothadi.chat.utils.AESUtils
import me.siddheshkothadi.chat.utils.RSAUtils

class MainViewModel : ViewModel() {
    private val auth = Firebase.auth

    val isSignedIn = MutableStateFlow(auth.currentUser != null)
    val isUserListLoading = MutableStateFlow(false)
    val areMessagesLoading = MutableStateFlow(false)
    val isSigningIn = MutableStateFlow(false)

    private val database = Firebase.database
    private val chatRef = database.getReference("chats")
    private val userRef = database.getReference("users")
    private val privateKeyRef = database.getReference("privateKeys")
    private val secretKeyRef = database.getReference("secretKeys")

    private val privateKey = MutableStateFlow<String>("")
    private val secretKey = MutableStateFlow<String>("")

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("91794365719-fn0c30j3sqfstf3bf50ilkbmtfs3crl0.apps.googleusercontent.com")
        .requestEmail()
        .build()

    private val _chats = MutableStateFlow<List<Message>>(listOf())
    val chats: Flow<List<Message>>
        get() = _chats.map { list ->
            list.map {
                val receiver = it.to
                val decryptionSecretKey =
                    if (receiver == auth.currentUser?.uid) RSAUtils.decrypt(
                        it.secretKey,
                        privateKey.value
                    ) else secretKey.value

                val decryptedText = AESUtils.decrypt(it.content, decryptionSecretKey)

                Message(
                    from = it.from,
                    to = it.to,
                    timestamp = it.timestamp,
                    content = decryptedText,
                )
            }.sortedByDescending { it.timestamp }
        }

    private val messageListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            areMessagesLoading.value = true
            val messages = dataSnapshot.getValue<List<Message>>()
            _chats.value = messages ?: listOf()
            areMessagesLoading.value = false
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("Message", "loadPost:onCancelled", databaseError.toException())
        }
    }

    val textState = mutableStateOf("")
    val users = mutableStateOf<List<User>>(listOf())

    init {
        auth.addAuthStateListener {
            isSigningIn.value = true
            Log.i("Auth", "Signed In State: ${it.currentUser != null}")
            it.currentUser?.let { userData ->
                userRef.child(userData.uid).get().addOnSuccessListener { data ->
                    if (data.value == null) {
                        val (publicKeyRSA, privateKeyRSA) = RSAUtils.getKeyPair()
                        val secretKeyAES = AESUtils.getSecretKey()

                        privateKey.value = privateKeyRSA
                        secretKey.value = secretKeyAES

                        val user = User(
                            uid = userData.uid,
                            displayName = userData.displayName.toString(),
                            email = userData.email.toString(),
                            photoUrl = userData.photoUrl.toString(),
                            publicKey = publicKeyRSA
                        )

                        userRef.child(userData.uid).setValue(user).addOnSuccessListener {
                            privateKeyRef.child(userData.uid).setValue(privateKeyRSA)
                                .addOnSuccessListener {
                                    secretKeyRef.child(userData.uid).setValue(secretKeyAES)
                                }
                        }
                    } else {
                        privateKeyRef.child(userData.uid).get()
                            .addOnSuccessListener { currentPrivateKeyDS ->
                                val currentPrivateKey =
                                    currentPrivateKeyDS.getValue(String::class.java)
                                if (currentPrivateKey != null) {
                                    privateKey.value = currentPrivateKey
                                    secretKeyRef.child(userData.uid).get()
                                        .addOnSuccessListener { currentSecretKeyDS ->
                                            val currentSecretKey =
                                                currentSecretKeyDS.getValue(String::class.java)
                                            if (currentSecretKey != null) {
                                                secretKey.value = currentSecretKey
                                            }
                                        }
                                }
                            }
                    }
                }
            }
            isSigningIn.value = false
            isSignedIn.value = (it.currentUser != null)
        }
    }

    fun addTextToChat(text: String, from: String, to: User, key: String) {
        if (text.isNotBlank()) {
            val chatList = _chats.value.toMutableList()
            val encryptedText = AESUtils.encrypt(text, secretKey.value)
            val encryptedSecretKey = RSAUtils.encrypt(secretKey.value, to.publicKey)

            chatList.add(
                Message(
                    from = from,
                    to = to.uid,
                    timestamp = System.currentTimeMillis().toString(),
                    content = encryptedText,
                    secretKey = encryptedSecretKey
                )
            )
            chatRef.child(key).setValue(chatList).addOnSuccessListener {
                textState.value = ""
            }.addOnCanceledListener {
                Log.e("MainViewModel", "Error")
            }
        }
    }

    fun setTextState(it: String) {
        textState.value = it
    }

    fun signWithCredential(credential: AuthCredential, context: Context) = viewModelScope.launch {
        try {
            isSigningIn.value = true
            Firebase.auth.signInWithCredential(credential).addOnSuccessListener {
                isSigningIn.value = false
                Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("Auth", e.toString())
            isSigningIn.value = false
        }
    }

    fun signOut() {
        auth.signOut()
        privateKey.value = ""
        secretKey.value = ""
    }

    fun fetchUsers() {
        isUserListLoading.value = true
        userRef.get().addOnSuccessListener { dataSnapshot ->
            users.value = dataSnapshot.children.map { child ->
                child.getValue(User::class.java)!!
            }.filter { filterUser ->
                auth.currentUser != null && auth.currentUser!!.uid != filterUser.uid
            }
            isUserListLoading.value = false
        }
    }

    fun getCurrentUserUid(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun addMessageEventListener(key: String) {
        chatRef.child(key).addValueEventListener(messageListener)
    }

    fun removeMessageEventListener(key: String) {
        chatRef.child(key).removeEventListener(messageListener)
    }

    fun getCurrentUser(): User {
        val displayName = auth.currentUser?.displayName ?: ""
        val photoUrl = auth.currentUser?.photoUrl.toString()
        val email = auth.currentUser?.email ?: ""

        return User(
            displayName = displayName,
            photoUrl = photoUrl,
            email = email
        )
    }
}