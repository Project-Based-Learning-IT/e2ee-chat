package me.siddheshkothadi.chat.model

import java.io.Serializable

data class Message(
    val from: String = "",
    val to: String = "",
    val timestamp: String = "",
    val content: String = ""
)
