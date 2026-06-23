package com.segunfrancis.newsfeed.ui.favourite

import com.segunfrancis.newsfeed.domain.DomainSavedArticle
import com.segunfrancis.newsfeed.util.toRelativeTimeString

data class SavedArticle(
    val url: String,
    val title: String,
    val author: String?,
    val source: String?,
    val content: String?,
    val imageUrl: String?,
    val description: String?,
    val publishedAt: String,
    val relativePublishedTime: String,
    val category: String,
    val savedAt: Long,
)

internal fun DomainSavedArticle.toSavedArticle(): SavedArticle {
    return SavedArticle(
        url = url,
        title = title,
        author = author,
        source = source,
        content = content,
        imageUrl = imageUrl,
        description = description,
        publishedAt = publishedAt,
        category = category,
        savedAt = savedAt,
        relativePublishedTime = publishedAt.toRelativeTimeString()
    )
}
