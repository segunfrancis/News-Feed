package com.segunfrancis.newsfeed.di

import android.content.Context
import androidx.work.WorkManager
import com.segunfrancis.newsfeed.data.local.NewsFeedDatabase
import com.segunfrancis.newsfeed.data.local.dao.NewsFeedDao
import com.segunfrancis.newsfeed.data.local.dao.SavedArticleDao
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import com.segunfrancis.newsfeed.data.remote.NewsFeedClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NewsFeedModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewsFeedDatabase? {
        return runBlocking { NewsFeedDatabase.getDatabase(context) }
    }

    @Provides
    fun provideDao(database: NewsFeedDatabase?): NewsFeedDao? {
        return database?.newaDao()
    }

    @Provides
    fun provideSavedArticleDao(database: NewsFeedDatabase?): SavedArticleDao? {
        return database?.savedArticleDao()
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

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
