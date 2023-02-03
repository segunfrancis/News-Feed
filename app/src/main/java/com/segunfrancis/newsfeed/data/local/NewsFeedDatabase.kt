package com.segunfrancis.newsfeed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.util.AppConstants.DATABASE_NAME
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(entities = [Article::class], exportSchema = true, version = 1)
abstract class NewsFeedDatabase : RoomDatabase() {

    abstract fun dao(): NewsFeedDao

    companion object {
        private var database: NewsFeedDatabase? = null
        private val mutex = Mutex()
        suspend fun getDatabase(context: Context): NewsFeedDatabase? {
            return database?.let {
                mutex.withLock { database }
            } ?: mutex.withLock {
                database =
                    Room.databaseBuilder(context, NewsFeedDatabase::class.java, DATABASE_NAME)
                        .build()
                database
            }
        }
    }
}
