package com.segunfrancis.newsfeed.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.segunfrancis.newsfeed.data.local.entities.Article
import com.segunfrancis.newsfeed.data.local.dao.NewsFeedDao
import com.segunfrancis.newsfeed.data.remote.NewsFeedApi
import com.segunfrancis.newsfeed.data.remote.isRemoved
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val category: String,
    private val dao: NewsFeedDao,
    private val api: NewsFeedApi,
    private val dispatcher: CoroutineDispatcher
) : RemoteMediator<Int, Article>() {

    companion object {
        private val CACHE_TTL = 48.hours.inWholeMilliseconds
    }

    /**
     * Called by Paging before the first [load] of each new PagingData.
     * This is where we decide whether to refresh on launch or skip straight
     * to observing the local database.
     *
     * Requirements handled here:
     *   Req 1 — fresh cache  → SKIP_INITIAL_REFRESH → Pager just emits from Room
     *   Req 2 — no local data → LAUNCH_INITIAL_REFRESH → triggers load(REFRESH)
     *   Req 3 — stale cache  → LAUNCH_INITIAL_REFRESH → triggers load(REFRESH)
     */
    override suspend fun initialize(): InitializeAction {
        val lastFetchTime = dao.getLastFetchTime(category) ?: 0L
        val cacheAge = System.currentTimeMillis() - lastFetchTime
        val isStale = cacheAge >= CACHE_TTL
        val hasNeverBeenFetched = cacheAge == 0L // No local data

        return if (isStale || hasNeverBeenFetched) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    /**
     * Called by Paging when it needs data. All four requirements converge here.
     *
     * LoadType.REFRESH — covers Req 2, 3, and 4:
     *   - On first load with no/stale data (from initialize above): clear + fetch + save
     *   - On manual refresh (lazyPagingItems.refresh()): check freshness again
     *       • Still stale  → clear + fetch + save   (Req 4, stale branch)
     *       • Now fresh    → fetch + save, no clear  (Req 4, not-stale branch)
     *
     * LoadType.PREPEND / APPEND — the API returns a full list per category,
     *   so there are no pages to prepend or append. Signal end of pagination.
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Article>
    ): MediatorResult {
        // This news feed is not paginated server-side (one request = full category list)
        if (loadType == LoadType.PREPEND || loadType == LoadType.APPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        // LoadType.REFRESH path — covers requirements 2, 3, and 4
        return try {
            val freshArticles = withContext(dispatcher) {
                api.getNews(category = category)
                    .articles
                    .map { it.copy(category = category) }
                    .filterNot { it.isRemoved() }
                    .map { it.toLocalArticle() }
            }
            // Re-check staleness at the point of writing.
            // This correctly differentiates Req 3 (auto stale refresh, always clear)
            // from Req 4 (manual refresh mid-session, clear only if stale).
            val lastFetchTime = dao.getLastFetchTime(category) ?: 0L
            val cacheIsStale = (System.currentTimeMillis() - lastFetchTime) >= CACHE_TTL
            if (cacheIsStale) {
                // Req 2 (first fetch) + Req 3 (stale) + Req 4 stale branch:
                // Remove outdated articles so the feed doesn't show removed stories
                dao.clearCategory(category)
            }
            // Req 4 not-stale branch: skip clear, just upsert — keeps existing
            // articles visible while network response streams in via the Flow
            dao.addNewsArticles(*freshArticles.toTypedArray())
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (t: Throwable) {
            // Paging surfaces this through lazyPagingItems.loadState.refresh
            // as a LoadState.Error — the UI can read it and show a retry button
            MediatorResult.Error(t)
        }
    }
}
