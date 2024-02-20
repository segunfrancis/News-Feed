package com.segunfrancis.newsfeed.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.segunfrancis.newsfeed.data.models.Article

@Dao
interface NewsFeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewsArticles(vararg article: Article)

    @Query("SELECT * FROM Article WHERE category is :category ORDER BY publishedAt DESC")
    fun getNewsArticles(category: String): PagingSource<Int, Article>

    @Query("SELECT * FROM Article ORDER BY publishedAt DESC")
    suspend fun getNewsArticlesForTesting(): List<Article>
}
