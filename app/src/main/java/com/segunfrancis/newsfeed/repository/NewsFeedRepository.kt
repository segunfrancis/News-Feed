package com.segunfrancis.newsfeed.repository

import kotlinx.coroutines.flow.Flow

interface NewsFeedRepository {

    suspend fun getNewsArticles(): Flow<RepositoryState>
}
