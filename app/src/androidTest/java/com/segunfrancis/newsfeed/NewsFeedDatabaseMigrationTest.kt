package com.segunfrancis.newsfeed

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.segunfrancis.newsfeed.data.local.NewsFeedDatabase
import com.segunfrancis.newsfeed.data.local.migrate_from_1_2
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.data.models.Source
import com.segunfrancis.newsfeed.util.article
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NewsFeedDatabaseMigrationTest {

    private val TEST_DB = "newsfeed-migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NewsFeedDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Before
    @Throws(Exception::class)
    fun setUp() {
        // To test migrations from version 1 of the database, we need to create the database
        // with version 1 using SQLite API

    }

    @Test
    @Throws(IOException::class)
    fun migrate1to2() = runTest {
        helper.createDatabase(TEST_DB, 1).apply {
            insertArticle(
                author = article.author,
                content = article.content,
                description = article.description,
                publishedAt = article.publishedAt,
                source = article.source,
                title = article.title,
                url = article.url,
                urlToImage = article.urlToImage,
                db = this
            )
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 2, true, migrate_from_1_2)
        getMigratedRoomDatabase().dao().addNewsArticles(article)
        val migratedArticle = getMigratedRoomDatabase().dao().getNewsArticlesForTesting()

        assertEquals(article.title, migratedArticle[0].title)
    }

    fun getMigratedRoomDatabase(): NewsFeedDatabase {
        return Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NewsFeedDatabase::class.java,
            TEST_DB
        ).addMigrations(migrate_from_1_2)
            .build().apply {
                openHelper.writableDatabase
                close()
            }
    }

    private fun insertArticle(
        author: String?,
        content: String?,
        description: String?,
        publishedAt: String,
        source: Source?,
        title: String,
        url: String,
        urlToImage: String?, db: SupportSQLiteDatabase
    ) {
        val values = ContentValues()
        values.put("title", title)
        values.put("publishedAt", publishedAt)
        values.put("url", url)
        values.put("author", author)
        values.put("content", content)
        values.put("description", description)
        values.put("urlToImage", urlToImage)
        values.put("id", source?.id)
        values.put("name", source?.name)

        db.insert("Article", SQLiteDatabase.CONFLICT_REPLACE, values)
    }

    private fun insertArticle(article: Article, db: SupportSQLiteDatabase) {
        val values = ContentValues()
        values.put("title", article.title)
        values.put("publishedAt", article.publishedAt)
        values.put("url", article.url)
        values.put("category", article.category)
        values.put("author", article.author)
        values.put("content", article.content)
        values.put("description", article.description)
        values.put("urlToImage", article.urlToImage)
        values.put("id", article.source?.id)
        values.put("name", article.source?.name)

        db.insert("Article", SQLiteDatabase.CONFLICT_REPLACE, values)
    }
}
