package com.segunfrancis.newsfeed.data.remote

data class NewsBaseResponseDto(
    val articles: List<ArticleDto>,
    val status: String,
    val totalResults: Int
)

data class SourceDto(
    val id: String?,
    val name: String
)

data class ArticleDto(
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String,
    val source: SourceDto?,
    val title: String,
    val url: String,
    val urlToImage: String?,
    val category: String,
    val fetchedAt: Long = System.currentTimeMillis(),
)

fun ArticleDto.isRemoved(): Boolean {
    return source?.name.equals("[Removed]", ignoreCase = true) || title.equals(
        "[Removed]",
        ignoreCase = true
    ) || description.equals("[Removed]", ignoreCase = true) || content.equals(
        "[Removed]",
        ignoreCase = true
    ) || url.equals("https://removed.com", ignoreCase = true)
}
