package com.segunfrancis.newsfeed.data

data class NewsBaseResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)