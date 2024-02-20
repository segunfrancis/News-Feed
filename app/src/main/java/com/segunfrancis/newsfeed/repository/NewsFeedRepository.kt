package com.segunfrancis.newsfeed.repository

import androidx.paging.Pager
import com.segunfrancis.newsfeed.data.models.Article
import kotlinx.coroutines.flow.Flow

interface NewsFeedRepository {

    suspend fun getNewsArticles(category: String): Pager<Int, Article>

    suspend fun loadNewsArticlesRemote(category: String): Flow<RepositoryRemoteState>

    suspend fun getNewsArticleTest(category: String): Flow<ResponseTest>
}
