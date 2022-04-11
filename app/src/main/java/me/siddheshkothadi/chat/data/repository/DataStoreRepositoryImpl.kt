package me.siddheshkothadi.chat.data.repository

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import me.siddheshkothadi.chat.ChatApplication
import me.siddheshkothadi.chat.data.UserData
import me.siddheshkothadi.chat.data.UserDataSerializer
import me.siddheshkothadi.chat.domain.model.User
import me.siddheshkothadi.chat.domain.repository.DataStoreRepository
import javax.inject.Inject


class DataStoreRepositoryImpl @Inject constructor(
    private val context: ChatApplication
) : DataStoreRepository {
    private val Context.dataStore by dataStore("user-data.json", UserDataSerializer)
    private val userDataStore = context.dataStore

    override val userData: Flow<UserData>
        get() = userDataStore.data

    override suspend fun updateUserData(
        user: User,
        privateKey: String,
        secretKey: String
    ): UserData {
        return userDataStore.updateData {
            UserData(
                user = user,
                privateKey = privateKey,
                secretKey = secretKey
            )
        }
    }
}