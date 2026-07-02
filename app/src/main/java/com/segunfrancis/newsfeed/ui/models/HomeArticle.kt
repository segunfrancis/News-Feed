package com.segunfrancis.newsfeed.ui.models

import com.segunfrancis.newsfeed.domain.DomainArticle
import com.segunfrancis.newsfeed.domain.DomainSavedArticle
import com.segunfrancis.newsfeed.util.formatDate
import com.segunfrancis.newsfeed.util.toRelativeTimeString

data class HomeArticle(
    val url: String,
    val title: String,
    val author: String?,
    val source: String?,
    val content: String?,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val formattedPublishedTime: String,
    val relativePublishedTime: String
)

internal fun DomainArticle.toHomeArticle(): HomeArticle {
    return HomeArticle(
        author = author,
        content = content,
        description = description,
        publishedAt = publishedAt,
        title = title,
        url = url,
        urlToImage = urlToImage,
        source = name,
        relativePublishedTime = publishedAt.toRelativeTimeString(),
        formattedPublishedTime = publishedAt.formatDate()
    )
}

fun HomeArticle.toDomainSavedArticle(category: String) = DomainSavedArticle(
    url = url,
    title = title,
    description = description,
    content = content,
    author = author,
    source = source,
    imageUrl = urlToImage,
    publishedAt = publishedAt,
    category = category,
    savedAt = System.currentTimeMillis(),
)
