package com.segunfrancis.newsfeed.repository

import com.segunfrancis.newsfeed.data.local.NewsFeedDao
import com.segunfrancis.newsfeed.data.models.Article
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject

class NewsFeedRepositoryImpl @Inject constructor(
    private val dao: NewsFeedDao?,
    private val api: NewsFeedApi,
    private val dispatcher: CoroutineDispatcher
) : NewsFeedRepository {
    override suspend fun getNewsArticles(): Flow<List<Article>> {
        return channelFlow {
            supervisorScope {
                val localDef = async {
                    send(dao?.getNewsArticles() ?: emptyList<Article>())
                }
                val remoteDef = async {
                    dao?.addNewsArticles(*api.getNews().articles.toTypedArray())
                    send(dao?.getNewsArticles() ?: emptyList<Article>())
                }
                runCatching { localDef.await() }.onFailure { Timber.e(it) }
                runCatching { remoteDef.await() }.onFailure { Timber.e(it) }
            }
        }.flowOn(dispatcher)
    }
}
