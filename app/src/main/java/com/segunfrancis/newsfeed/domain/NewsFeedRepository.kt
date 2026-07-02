package com.segunfrancis.newsfeed.domain

import androidx.paging.PagingData
import com.segunfrancis.newsfeed.data.ResponseTest
import kotlinx.coroutines.flow.Flow

interface NewsFeedRepository {

    fun getNewsArticles(category: String): Flow<PagingData<DomainArticle>>

    suspend fun getNewsArticleTest(category: String): Flow<ResponseTest>

    suspend fun prefetchAll(categories: List<String>): List<Result<Unit>>

    suspend fun searchNews(query: String): List<DomainArticle>
}
