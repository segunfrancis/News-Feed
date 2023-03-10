package com.segunfrancis.newsfeed.usecase

import com.segunfrancis.newsfeed.data.models.Article

data class HomeResponse(
    val articles: List<Article> = emptyList(),
    val networkLoading: Boolean = false,
    val error: Throwable? = null
)
