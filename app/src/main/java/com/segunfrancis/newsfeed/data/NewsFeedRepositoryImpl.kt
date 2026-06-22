package com.segunfrancis.newsfeed.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.segunfrancis.newsfeed.data.local.entities.Article
import com.segunfrancis.newsfeed.data.local.dao.NewsFeedDao
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import com.segunfrancis.newsfeed.data.remote.isRemoved
import com.segunfrancis.newsfeed.domain.DomainArticle
import com.segunfrancis.newsfeed.domain.NewsFeedRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class NewsFeedRepositoryImpl @Inject constructor(
    private val dao: NewsFeedDao?,
    private val api: NewsFeedApi,
    private val dispatcher: CoroutineDispatcher
) : NewsFeedRepository {

    /**
     * What callers get back:
     *   • A Flow<PagingData<DomainArticle>> that always emits from Room (Req 1)
     *   • Automatic network refresh handled by [NewsRemoteMediator] (Req 2, 3, 4)
     *   • Manual refresh triggered by calling `.refresh()` on LazyPagingItems in the UI
     *
     * No longer `suspend` — the Flow is constructed synchronously and the Pager
     * starts collecting only when the caller collects, so no suspension is needed here.
     */
    override fun getNewsArticles(category: String): Flow<PagingData<DomainArticle>> {
        val safeDao = dao ?: throw IllegalStateException(
            "Unable to access database. Clear app data and try again."
        )

        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = true,
                maxSize = 150,
                // Prevent Room from triggering spurious re-fetches on minor DB writes
                // (e.g., when upsert touches a fetchedAt field but not meaningful content)
                jumpThreshold = 100
            ),
            // The mediator handles all network coordination defined above
            remoteMediator = NewsRemoteMediator(
                category = category,
                dao = safeDao,
                api = api,
                dispatcher = dispatcher
            ),
            // Room PagingSource — the single source of truth the UI always reads from.
            // Whenever the mediator writes to Room (after a network fetch), this source
            // is invalidated and re-emits automatically. No manual trigger needed.
            pagingSourceFactory = { dao.getNewsArticles(category) }
        ).flow.map { pagingData ->
            pagingData.map { article ->
                article.toDomainArticle()
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
                        .map { it.toLocalArticle() }
                dao?.addNewsArticles(*networkResponse.toTypedArray())
            } catch (t: Throwable) {
                emit(ResponseTest.Error(t))
            }
        }.flowOn(dispatcher)
    }

    /**
     * Optional: prefetch all categories in parallel.
     *
     * Use this if you want to warm the cache eagerly on app start (e.g. in a
     * WorkManager task or when the user has a fast connection).
     *
     * coroutineScope {} makes all async {} calls fail-fast together.
     * Use supervisorScope {} instead if you want one failure to NOT cancel others.
     */
    override suspend fun prefetchAll(categories: List<String>) = coroutineScope {
        categories
            .map { category -> async { runCatching { fetchAndCache(category) } } }
            .awaitAll()
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private suspend fun fetchAndCache(category: String) {
        val articles = api.getNews(category = category).articles
            .map { it.copy(category = category) }
            .filterNot { it.isRemoved() }
            .map { it.toLocalArticle() }
        val dao = dao ?: return
        dao.clearCategory(category)
        dao.addNewsArticles(*articles.toTypedArray())
    }
}

sealed interface ResponseTest {
    data class Success(val data: Pager<Int, Article>) : ResponseTest
    data class Error(val t: Throwable) : ResponseTest
}
