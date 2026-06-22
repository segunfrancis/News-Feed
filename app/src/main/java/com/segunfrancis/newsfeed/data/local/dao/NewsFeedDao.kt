package com.segunfrancis.newsfeed.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.segunfrancis.newsfeed.data.local.entities.Article

@Dao
interface NewsFeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewsArticles(vararg article: Article)

    @Query("SELECT * FROM Article WHERE category is :category ORDER BY publishedAt DESC")
    fun getNewsArticles(category: String): PagingSource<Int, Article>

    @Query("SELECT * FROM Article ORDER BY publishedAt DESC")
    suspend fun getNewsArticlesForTesting(): List<Article>

    /** Called before inserting a fresh network response to avoid stale articles. */
    @Query("DELETE FROM Article WHERE category = :category")
    suspend fun clearCategory(category: String)

    /**
     * The repository checks this before hitting the network.
     * Returns null if the category has never been fetched.
     */
    @Query("SELECT MAX(fetchedAt) FROM Article WHERE category = :category")
    suspend fun getLastFetchTime(category: String): Long?
}
