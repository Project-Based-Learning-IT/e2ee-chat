package me.siddheshkothadi.chat.domain.repository

import kotlinx.coroutines.flow.Flow
import me.siddheshkothadi.chat.data.UserData
import me.siddheshkothadi.chat.domain.model.User

interface DataStoreRepository {
    val userData: Flow<UserData>

    suspend fun updateUserData(
        user: User,
        privateKey: String,
        secretKey: String
    ): UserData
}