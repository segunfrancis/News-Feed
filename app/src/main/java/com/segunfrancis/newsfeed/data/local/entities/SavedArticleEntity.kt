package com.segunfrancis.newsfeed.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SavedArticle")
data class SavedArticleEntity(
    @PrimaryKey
    val url: String,
    val title: String,
    val description: String?,
    val content: String?,
    val author: String?,
    val source: String?,
    val imageUrl: String?,
    val publishedAt: String,
    val category: String,
    val savedAt: Long = System.currentTimeMillis(),
)
