package com.segunfrancis.newsfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.segunfrancis.newsfeed.data.local.entities.SavedArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(article: SavedArticleEntity)

    @Query("DELETE FROM SavedArticle WHERE url = :url")
    suspend fun remove(url: String)

    /**
     * Returns 1 if saved, 0 if not. Mapped to Boolean in the repository.
     * Room re-emits whenever the saved_articles table changes, so the
     * bookmark icon updates instantly without any manual refresh.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM SavedArticle WHERE url = :url LIMIT 1)")
    fun isSaved(url: String): Flow<Boolean>

    @Query("SELECT * FROM SavedArticle ORDER BY savedAt DESC")
    fun getAllSaved(): Flow<List<SavedArticleEntity>>

    /**
     * Loads only URLs — a lightweight query that feeds the feed-level
     * bookmark icon state without pulling full article rows.
     */
    @Query("SELECT url FROM SavedArticle")
    fun getSavedUrls(): Flow<List<String>>
}
