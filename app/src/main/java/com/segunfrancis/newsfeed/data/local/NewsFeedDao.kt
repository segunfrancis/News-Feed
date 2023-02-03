package com.segunfrancis.newsfeed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.segunfrancis.newsfeed.data.models.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsFeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewsArticles(vararg article: Article)

    @Query("SELECT * FROM Article")
    suspend fun getNewsArticles(): Flow<List<Article>>
}
