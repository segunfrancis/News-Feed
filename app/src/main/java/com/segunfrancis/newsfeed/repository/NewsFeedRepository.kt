package com.segunfrancis.newsfeed.repository

import com.segunfrancis.newsfeed.data.models.Article
import kotlinx.coroutines.flow.Flow

interface NewsFeedRepository {

    suspend fun getNewsArticles(): Flow<List<Article>>
}
