package com.segunfrancis.newsfeed.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.segunfrancis.newsfeed.data.local.dao.NewsFeedDao
import com.segunfrancis.newsfeed.data.local.dao.SavedArticleDao
import com.segunfrancis.newsfeed.data.local.entities.Article
import com.segunfrancis.newsfeed.data.local.entities.SavedArticleEntity
import com.segunfrancis.newsfeed.util.AppConstants.DATABASE_NAME
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(entities = [Article::class, SavedArticleEntity::class], exportSchema = true, version = 4)
abstract class NewsFeedDatabase : RoomDatabase() {

    abstract fun newaDao(): NewsFeedDao
    abstract fun savedArticleDao(): SavedArticleDao

    companion object {
        private var database: NewsFeedDatabase? = null
        private val mutex = Mutex()
        suspend fun getDatabase(context: Context): NewsFeedDatabase? {
            return try {
                database?.let {
                    mutex.withLock { database }
                } ?: mutex.withLock {
                    database =
                        Room.databaseBuilder(context, NewsFeedDatabase::class.java, DATABASE_NAME)
                            .addMigrations(migrate_from_1_2)
                            .addMigrations(migrate_from_2_3)
                            .addMigrations(migrate_from_3_4)
                            .build()
                    database
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
    }
}

@VisibleForTesting
val migrate_from_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Article` ADD COLUMN `category` TEXT")
    }
}

@VisibleForTesting
val migrate_from_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Article` ADD COLUMN fetchedAt INTEGER NOT NULL DEFAULT 0")
    }
}

@VisibleForTesting
val migrate_from_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS SavedArticle (
                    url         TEXT    NOT NULL PRIMARY KEY,
                    title       TEXT    NOT NULL,
                    description TEXT,
                    content     TEXT,
                    author      TEXT,
                    source      TEXT,
                    imageUrl    TEXT,
                    publishedAt TEXT    NOT NULL,
                    category    TEXT    NOT NULL,
                    savedAt     INTEGER NOT NULL
                )
            """.trimIndent()
        )
    }
}
