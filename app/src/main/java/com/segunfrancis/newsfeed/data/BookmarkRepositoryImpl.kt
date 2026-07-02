package com.segunfrancis.newsfeed.data

import com.segunfrancis.newsfeed.data.local.dao.SavedArticleDao
import com.segunfrancis.newsfeed.domain.BookmarkRepository
import com.segunfrancis.newsfeed.domain.DomainSavedArticle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val dao: SavedArticleDao?,
    private val dispatcher: CoroutineDispatcher
) : BookmarkRepository {

    override suspend fun bookmark(article: DomainSavedArticle) {
        val safeDao = dao ?: throw IllegalStateException(
            "Unable to access database. Clear app data and try again."
        )
        withContext(dispatcher) { safeDao.save(article.toEntity()) }
    }

    override suspend fun removeBookmark(url: String) {
        val safeDao = dao ?: throw IllegalStateException(
            "Unable to access database. Clear app data and try again."
        )
        withContext(dispatcher) { safeDao.remove(url) }
    }

    override fun isBookmarked(url: String): Flow<Boolean> {
        val safeDao = dao ?: throw IllegalStateException(
            "Unable to access database. Clear app data and try again."
        )
        return safeDao.isSaved(url).flowOn(dispatcher)
    }

    override fun getAllBookmarks(): Flow<List<DomainSavedArticle>> {
        val safeDao = dao ?: throw IllegalStateException(
            "Unable to access database. Clear app data and try again."
        )
        return safeDao.getAllSaved()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)
    }

    override fun getBookmarkedUrls(): Flow<Set<String>> {
        val safeDao = dao ?: throw IllegalStateException(
            "Unable to access database. Clear app data and try again."
        )
        return safeDao.getSavedUrls()
            .map { it.toSet() }
            .flowOn(dispatcher)
    }
}
