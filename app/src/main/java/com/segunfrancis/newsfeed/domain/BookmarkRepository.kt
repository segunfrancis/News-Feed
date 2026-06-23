package com.segunfrancis.newsfeed.domain

import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    /**
     * Persists an article to the saved table.
     * Safe to call if the article is already saved — REPLACE strategy handles it.
     */
    suspend fun bookmark(article: DomainSavedArticle)

    /**
     * Removes a saved article by its URL.
     * No-op if the article was never saved.
     */
    suspend fun removeBookmark(url: String)

    /**
     * Emits true when the given URL is in the saved table, false otherwise.
     * Re-emits automatically when the saved state changes — no manual polling.
     *
     * Use in an article detail screen or card to drive a filled/unfilled icon.
     */
    fun isBookmarked(url: String): Flow<Boolean>

    /**
     * Emits all saved articles ordered by most recently saved first.
     * Re-emits on every insert or delete so the saved screen stays live.
     */
    fun getAllBookmarks(): Flow<List<DomainSavedArticle>>

    /**
     * Emits the set of saved URLs.
     *
     * Prefer this over calling isBookmarked() per article in a list — one
     * query feeds the entire feed. In the ViewModel, collect this as a
     * StateFlow<Set<String>> and check `url in savedUrls` per card.
     */
    fun getBookmarkedUrls(): Flow<Set<String>>
}
