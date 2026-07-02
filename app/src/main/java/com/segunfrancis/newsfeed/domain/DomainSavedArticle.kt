package com.segunfrancis.newsfeed.domain

/**
 * Domain representation of a saved article.
 *
 * Mirrors the fields of DomainArticle so the presentation layer can treat them
 * the same way. [savedAt] is domain-relevant because saved articles are
 * typically displayed in the order they were bookmarked.
 */
data class DomainSavedArticle(
    val url: String,
    val title: String,
    val description: String?,
    val content: String?,
    val author: String?,
    val source: String?,
    val imageUrl: String?,
    val publishedAt: String,
    val category: String,
    val savedAt: Long,
)
