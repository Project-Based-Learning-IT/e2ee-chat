package me.siddheshkothadi.chat.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.siddheshkothadi.chat.ChatApplication
import me.siddheshkothadi.chat.data.UserData
import me.siddheshkothadi.chat.domain.model.Message
import me.siddheshkothadi.chat.domain.model.User
import me.siddheshkothadi.chat.domain.repository.DataStoreRepository
import me.siddheshkothadi.chat.utils.AESUtils
import me.siddheshkothadi.chat.utils.RSAUtils
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val context: ChatApplication,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val auth = Firebase.auth
    private val database = Firebase.database

    private val userData = MutableStateFlow(UserData())

    val isSignedIn = MutableStateFlow(auth.currentUser != null)
    val isUserListLoading = MutableStateFlow(false)
    val areMessagesLoading = MutableStateFlow(false)
    val textState = mutableStateOf("")
    val users = mutableStateOf<List<User>>(emptyList())
    val isSigningIn = MutableStateFlow(false)

    private val chatRef = database.getReference("chats")
    private val userRef = database.getReference("users")

    private val clientIDWeb =
        "91794365719-fn0c30j3sqfstf3bf50ilkbmtfs3crl0.apps.googleusercontent.com"

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientIDWeb)
        .requestEmail()
        .build()

    private val _chats = MutableStateFlow<List<Message>>(emptyList())
    val chats: Flow<List<Message>>
        get() = _chats.map { list ->
            list.map {
                val receiver = it.to
                val decryptionSecretKey =
                    if (receiver == auth.currentUser?.uid) RSAUtils.decrypt(
                        it.secretKey,
                        userData.value.privateKey
                    ) else userData.value.secretKey

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
            _chats.value = messages ?: emptyList()
            areMessagesLoading.value = false
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("onCancelled", "loadPost:onCancelled", databaseError.toException())
        }
    }

    init {
        auth.addAuthStateListener {
            viewModelScope.launch {
                isSigningIn.value = true
                dataStoreRepository.userData.first().let { userDataFromDataStore ->
                    if (userDataFromDataStore.privateKey.isEmpty()) {
                        // Not previously logged in, does not contain userData
                        it.currentUser?.let { firebaseUser ->
                            // If just logged in, delete old data and save new data
                            deleteChatsOfUser(firebaseUser.uid) {
                                deleteUser(firebaseUser.uid) {
                                    saveNewUser(firebaseUser)
                                }
                            }
                        }
                    } else {
                        userData.value = userDataFromDataStore
                    }
                }
                isSigningIn.value = false
                isSignedIn.value = (it.currentUser != null)
            }
        }
    }

    fun addTextToChat(text: String, from: String, to: User, key: String) {
        if (text.isNotBlank()) {
            areMessagesLoading.value = true
            val chatList = _chats.value.toMutableList()
            val encryptedText = AESUtils.encrypt(text, userData.value.secretKey)
            val encryptedSecretKey = RSAUtils.encrypt(userData.value.secretKey, to.publicKey)

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
                areMessagesLoading.value = false
            }.addOnCanceledListener {
                Log.e("MainViewModel", "Error")
                areMessagesLoading.value = false
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

    fun signOut(callback: () -> Unit) {
        GoogleSignIn.getClient(context, gso).signOut().addOnSuccessListener {
            deleteChatsOfUser(userData.value.user.uid) {
                deleteUser(userData.value.user.uid) {
                    updateUserData()
                    auth.signOut()
                    callback()
                    Toast.makeText(context, "Logged Out", Toast.LENGTH_LONG).show()
                }
            }
        }
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

    fun getCurrentUserUid(): String = userData.value.user.uid

    fun addMessageEventListener(key: String) {
        chatRef.child(key).addValueEventListener(messageListener)
    }

    fun removeMessageEventListener(key: String) {
        chatRef.child(key).removeEventListener(messageListener)
    }

    fun getCurrentUser(): User = userData.value.user

    fun clearChats() {
        _chats.value = emptyList()
    }

    private fun deleteUser(uid: String, callback: () -> Unit) {
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val filteredUsers = dataSnapshot.children.map {
                it.getValue(User::class.java)
            }.filter {
                it?.uid != uid
            }

            userRef.setValue(filteredUsers).addOnSuccessListener {
                callback()
            }
        }
    }

    private fun deleteChatsOfUser(uid: String, callback: () -> Unit) {
        chatRef.get().addOnSuccessListener { dataSnapshot ->
            val hashMap: HashMap<String, List<Message>> = hashMapOf()

            for (item in dataSnapshot.children) {
                item.key?.let {
                    if (!it.contains(uid)) {
                        hashMap[it] = item.value as List<Message>
                    }
                }
            }

            chatRef.setValue(hashMap).addOnSuccessListener {
                callback()
            }
        }
    }

    private fun saveNewUser(firebaseUser: FirebaseUser) {
        val (publicKeyRSA, privateKeyRSA) = RSAUtils.getKeyPair()
        val secretKeyAES = AESUtils.getSecretKey()

        val user = User(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName.toString(),
            email = firebaseUser.email.toString(),
            photoUrl = firebaseUser.photoUrl.toString(),
            publicKey = publicKeyRSA
        )

        userRef.child(firebaseUser.uid).setValue(user).addOnSuccessListener {
            updateUserData(user, privateKeyRSA, secretKeyAES)
        }
    }

    private fun updateUserData(
        user: User = User(),
        privateKey: String = "",
        secretKey: String = "",
    ) {
        viewModelScope.launch {
            userData.value = dataStoreRepository.updateUserData(user, privateKey, secretKey)
        }
    }
}