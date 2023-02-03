package com.segunfrancis.newsfeed.di

import android.content.Context
import com.segunfrancis.newsfeed.data.local.NewsFeedDao
import com.segunfrancis.newsfeed.data.local.NewsFeedDatabase
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import com.segunfrancis.newsfeed.data.remote.NewsFeedClient
import com.segunfrancis.newsfeed.repository.NewsFeedRepository
import com.segunfrancis.newsfeed.repository.NewsFeedRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NewsFeedModule {

    @Provides
    @Singleton
    suspend fun provideDatabase(@ApplicationContext context: Context): NewsFeedDatabase? {
        return NewsFeedDatabase.getDatabase(context)
    }

    @Provides
    fun provideDao(database: NewsFeedDatabase): NewsFeedDao {
        return database.dao()
    }

    @Provides
    @Singleton
    fun provideRemoteClient(): NewsFeedApi {
        return NewsFeedClient.getApi()
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface OtherModule {

    @Binds
    fun bindRepository(repositoryImpl: NewsFeedRepositoryImpl): NewsFeedRepository
}
