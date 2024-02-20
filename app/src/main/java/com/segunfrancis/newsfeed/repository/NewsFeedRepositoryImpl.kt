package com.segunfrancis.newsfeed.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.segunfrancis.newsfeed.data.local.NewsFeedDao
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import com.segunfrancis.newsfeed.util.isRemoved
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class NewsFeedRepositoryImpl @Inject constructor(
    private val dao: NewsFeedDao?,
    private val api: NewsFeedApi,
    private val dispatcher: CoroutineDispatcher
) : NewsFeedRepository {
    override suspend fun getNewsArticles(category: String): Pager<Int, Article> {
        return if (dao != null) {
            Pager(config = PagingConfig(pageSize = 50, enablePlaceholders = true, maxSize = 150)) {
                dao.getNewsArticles(category = category)
            }
        } else {
            throw Exception("Unable to access database. Clear app data and try again")
        }
    }

    override suspend fun loadNewsArticlesRemote(category: String): Flow<RepositoryRemoteState> {
        val state = RepositoryRemoteState()
        return flow {
            emit(state.copy(networkLoading = true))
            try {
                val networkResponse =
                    api.getNews(category = category).articles.map { it.copy(category = category) }
                        .filterNot { it.isRemoved() }
                dao?.addNewsArticles(*networkResponse.toTypedArray())
                emit(state.copy(networkLoading = false))
            } catch (t: Throwable) {
                emit(state.copy(error = t))
            }
        }.flowOn(dispatcher)
    }

    override suspend fun getNewsArticleTest(category: String): Flow<ResponseTest> {
        return flow {
            emit(
                ResponseTest.Success(
                    Pager(
                        config = PagingConfig(
                            pageSize = 50,
                            enablePlaceholders = true,
                            maxSize = 150
                        )
                    ) {
                        dao!!.getNewsArticles(category = category)
                    }
                )
            )
            try {
                val networkResponse =
                    api.getNews(category = category).articles.map { it.copy(category = category) }
                        .filterNot { it.isRemoved() }
                dao?.addNewsArticles(*networkResponse.toTypedArray())
            } catch (t: Throwable) {
                emit(ResponseTest.Error(t))
            }
        }.flowOn(dispatcher)
    }
}

sealed interface ResponseTest {
    data class Success(val data: Pager<Int, Article>) : ResponseTest
    data class Error(val t: Throwable) : ResponseTest
}