package me.siddheshkothadi.chat

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import me.siddheshkothadi.chat.data.Message

class MainViewModel: ViewModel() {
    private val database = Firebase.database
    private val chatRef = database.getReference("chat")

    private val _chats = MutableLiveData(emptyList<Message>().toMutableList())
    val chats: LiveData<MutableList<Message>>
        get() = _chats

    val textState = mutableStateOf("")

    init {
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<MutableList<Message>>() ?: listOf<Message>().toMutableList()
                Log.d("MainViewModel", "Value is: $value")

                _chats.value = value.asReversed()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainViewModel", "Failed to read value.", error.toException())
            }
        })
    }

    fun addTextToChat(text: String, name: String) {
        val chatList = _chats.value ?: emptyList<Message>().toMutableList()
        chatList.add(Message(
            from = name,
            to = if(name == "Alice") "Bob" else "Alice",
            timestamp = System.currentTimeMillis().toString(),
            content = text
        ))
        chatRef.setValue(chatList.sortByDescending { it.timestamp }).addOnSuccessListener {
            textState.value = ""
        }.addOnCanceledListener {
            Log.e("MainViewModel", "Error")
        }
    }

    fun setTextState(it: String) {
        textState.value = it
    }
}