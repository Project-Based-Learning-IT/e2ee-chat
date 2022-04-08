package me.siddheshkothadi.chat

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
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
import java.security.KeyPairGenerator

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

    private val currentUserPrivateKey = MutableStateFlow<String>("")
    private val otherUserPrivateKey = MutableStateFlow<String>("")

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("91794365719-fn0c30j3sqfstf3bf50ilkbmtfs3crl0.apps.googleusercontent.com")
        .requestEmail()
        .build()

    private val _chats = MutableStateFlow<List<Message>>(listOf())
    val chats: Flow<List<Message>>
        get() = _chats.map { list ->
            list.map {
                val receiver = it.to
                val privateKey = if(receiver == auth.currentUser?.uid) currentUserPrivateKey.value else otherUserPrivateKey.value
                val decryptedText = RSAUtils.decrypt(it.content, privateKey)

                Message(
                    from = it.from,
                    to = it.to,
                    timestamp = it.timestamp,
                    content = decryptedText
                )
            }.sortedByDescending { it.timestamp }
        }

    private val messageListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            areMessagesLoading.value = true
            val messages = dataSnapshot.getValue<List<Message>>()

            if (messages.isNullOrEmpty()) {
                _chats.value = listOf()
            } else {
                val currUserUid = auth.currentUser?.uid
                val otherUserUid =
                    if (messages[0].to == currUserUid) messages[0].from else messages[0].to

                if (currUserUid != null) {
                    privateKeyRef.child(currUserUid).get().addOnSuccessListener { currentPrivateKeyDS ->
                        val currPrivateKey = currentPrivateKeyDS.getValue(String::class.java)
                        if (currPrivateKey != null) {
                            currentUserPrivateKey.value = currPrivateKey
                            privateKeyRef.child(otherUserUid).get().addOnSuccessListener { otherPrivateKeyDS ->
                                val otherPrivateKey = otherPrivateKeyDS.getValue(String::class.java)
                                if (otherPrivateKey != null) {
                                    otherUserPrivateKey.value = otherPrivateKey
                                    _chats.value = messages
                                }
                            }
                        }
                    }
                }
            }
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
                Log.i("Auth", userData.email.toString())
                userRef.child(userData.uid).get().addOnSuccessListener { data ->
                    if (data.value == null) {
                        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                        val keyPair = keyPairGenerator.generateKeyPair()
                        val publicKey = RSAUtils.keyToEncodedString(keyPair.public)
                        val privateKey = RSAUtils.keyToEncodedString(keyPair.private)

                        val user = User(
                            uid = userData.uid,
                            displayName = userData.displayName.toString(),
                            email = userData.email.toString(),
                            photoUrl = userData.photoUrl.toString(),
                            publicKey = publicKey
                        )

                        userRef.child(userData.uid).setValue(user).addOnSuccessListener {
                            privateKeyRef.child(userData.uid).setValue(privateKey)
                        }
                    }
                }
            }
            isSigningIn.value = false
            isSignedIn.value = (it.currentUser != null)
        }
    }

    fun addTextToChat(text: String, from: String, to: User, key: String) {
        val chatList = _chats.value.toMutableList()

        val letters = text.split("")
        val listOfText = letters.chunked(85).map {
            it.joinToString("")
        }

        listOfText.forEach {
            if (it.isNotBlank()) {
                val encryptedText = RSAUtils.encrypt(it, to.publicKey)

                chatList.add(
                    Message(
                        from = from,
                        to = to.uid,
                        timestamp = System.currentTimeMillis().toString(),
                        content = encryptedText
                    )
                )
            }
        }

        chatRef.child(key).setValue(chatList).addOnSuccessListener {
            textState.value = ""
        }.addOnCanceledListener {
            Log.e("MainViewModel", "Error")
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
        val photoUrl = auth.currentUser?.photoUrl.toString() ?: ""
        val email = auth.currentUser?.email ?: ""

        return User(
            displayName = displayName,
            photoUrl = photoUrl,
            email = email
        )
    }
}