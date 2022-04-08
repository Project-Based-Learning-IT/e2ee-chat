package me.siddheshkothadi.chat.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val publicKey: String = ""
)
