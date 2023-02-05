package com.segunfrancis.newsfeed.ui.models

data class HomeArticle(
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val title: String,
    val url: String,
    val urlToImage: String,
    val onClick:(HomeArticle) -> Unit
)
