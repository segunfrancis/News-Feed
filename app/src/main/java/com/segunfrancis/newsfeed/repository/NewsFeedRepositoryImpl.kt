package com.segunfrancis.newsfeed.repository

import com.segunfrancis.newsfeed.data.local.NewsFeedDao
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class NewsFeedRepositoryImpl @Inject constructor(
    private val dao: NewsFeedDao?,
    private val api: NewsFeedApi,
    private val dispatcher: CoroutineDispatcher
) : NewsFeedRepository {
    override suspend fun getNewsArticles(): Flow<RepositoryState> {
        var response = RepositoryState()
        return channelFlow {
            supervisorScope {
                try {
                    response = response.copy(
                        articles = dao?.getNewsArticles() ?: emptyList()
                    )
                    send(response)
                } catch (t: Throwable) {
                    response = response.copy(error = t)
                    send(response)
                }
                try {
                    response = response.copy(networkLoading = true)
                    send(response)
                    dao?.addNewsArticles(*api.getNews().articles.toTypedArray())
                    response = response.copy(
                        articles = dao?.getNewsArticles() ?: emptyList(),
                        networkLoading = false
                    )
                    send(response)
                } catch (t: Throwable) {
                    response = response.copy(error = t, networkLoading = false)
                    send(response)
                }
            }
        }.flowOn(dispatcher)
    }
}
