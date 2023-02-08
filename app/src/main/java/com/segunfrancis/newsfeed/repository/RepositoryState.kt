package com.segunfrancis.newsfeed.repository

import com.segunfrancis.newsfeed.data.models.Article

data class RepositoryState(
    val articles: List<Article> = emptyList(),
    val networkLoading: Boolean = false,
    val error: Throwable? = null
)
