package com.segunfrancis.newsfeed.repository

import com.segunfrancis.newsfeed.data.local.NewsFeedDao
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class NewsFeedRepositoryImpl @Inject constructor(
    private val dao: NewsFeedDao,
    private val api: NewsFeedApi,
    private val dispatcher: CoroutineDispatcher
) : NewsFeedRepository {
    override suspend fun getNewsArticles(): Flow<List<Article>> {
        return flow {
            emit(dao.getNewsArticles().first())
            dao.addNewsArticles(*api.getNews().articles.toTypedArray())
        }.flowOn(dispatcher)
    }
}
