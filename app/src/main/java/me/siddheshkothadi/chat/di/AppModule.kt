package me.siddheshkothadi.chat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.siddheshkothadi.chat.ChatApplication
import me.siddheshkothadi.chat.data.repository.DataStoreRepositoryImpl
import me.siddheshkothadi.chat.domain.repository.DataStoreRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext context: Context): ChatApplication {
        return context as ChatApplication
    }

    @Singleton
    @Provides
    fun provideDataStoreRepository(context: ChatApplication): DataStoreRepository {
        return DataStoreRepositoryImpl(context)
    }
}