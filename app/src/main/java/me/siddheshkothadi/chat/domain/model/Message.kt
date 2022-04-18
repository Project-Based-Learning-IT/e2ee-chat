package me.siddheshkothadi.chat.domain.model

data class Message(
    val from: String = "",
    val to: String = "",
    val timestamp: String = "",
    val date: String = "",
    val time: String = "",
    val content: String = "",
    val secretKey: String = ""
)
