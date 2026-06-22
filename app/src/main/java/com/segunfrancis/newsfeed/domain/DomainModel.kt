package com.segunfrancis.newsfeed.domain

data class DomainArticle(
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String,
    val id: String?,
    val name: String,
    val title: String,
    val url: String,
    val urlToImage: String?,
    val category: String,
    val fetchedAt: Long
)
