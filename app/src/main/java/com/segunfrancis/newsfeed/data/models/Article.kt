package com.segunfrancis.newsfeed.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Article(
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String,
    @Embedded val source: Source?,
    @PrimaryKey val title: String,
    val url: String,
    val urlToImage: String?,
    val category: String
)
