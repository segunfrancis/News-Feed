package com.segunfrancis.newsfeed.usecase

import com.segunfrancis.newsfeed.data.models.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class HomeResponse(
    val articlesFlow: Flow<List<Article>> = flowOf(emptyList()),
    val error: Throwable? = null
)
