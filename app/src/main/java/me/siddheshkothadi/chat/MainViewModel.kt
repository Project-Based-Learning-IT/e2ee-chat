package me.siddheshkothadi.chat

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import me.siddheshkothadi.chat.model.Message

class MainViewModel : ViewModel() {
    private val database = Firebase.database
    private val chatRef = database.getReference("chat")

    private val _chats = MutableLiveData(listOf<Message>())
    val chats: LiveData<List<Message>>
        get() {
            return Transformations.map(_chats) { chatList ->
                chatList.map {
                    val receiver = it.to
                    val decryptionKey = if (receiver == "Alice") alicePrivate else bobPrivate
                    val decryptedContent = RSAUtils.decrypt(it.content, decryptionKey)

                    Message(it.from, it.to, it.timestamp, decryptedContent)
                }.sortedByDescending { it.timestamp }
            }
        }

    val textState = mutableStateOf("")

    init {
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value =
                    snapshot.getValue<MutableList<Message>>() ?: listOf<Message>().toMutableList()
                Log.d("MainViewModel", "Value is: $value")

                _chats.value = value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainViewModel", "Failed to read value.", error.toException())
            }
        })
    }

    fun addTextToChat(text: String, name: String) {
        val chatList = _chats.value?.toMutableList() ?: emptyList<Message>().toMutableList()
        val encryptionKey = if (name == "Alice") alicePublic else bobPublic

        val letters = text.split("")
        val listOfText = letters.chunked(85).map {
            it.joinToString("")
        }

        listOfText.forEach {
            if(it.isNotBlank()) {
                val encryptedText = RSAUtils.encrypt(it, encryptionKey)

                chatList.add(
                    Message(
                        from = if (name == "Alice") "Bob" else "Alice",
                        to = name,
                        timestamp = System.currentTimeMillis().toString(),
                        content = encryptedText
                    )
                )
            }
        }

        chatRef.setValue(chatList).addOnSuccessListener {
            textState.value = ""
        }.addOnCanceledListener {
            Log.e("MainViewModel", "Error")
        }
    }

    fun setTextState(it: String) {
        textState.value = it
    }
}