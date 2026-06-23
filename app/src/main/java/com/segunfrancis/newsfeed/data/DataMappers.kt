package com.segunfrancis.newsfeed.data

import com.segunfrancis.newsfeed.data.local.entities.Article
import com.segunfrancis.newsfeed.data.local.entities.SavedArticleEntity
import com.segunfrancis.newsfeed.data.local.entities.Source
import com.segunfrancis.newsfeed.data.remote.ArticleDto
import com.segunfrancis.newsfeed.data.remote.SourceDto
import com.segunfrancis.newsfeed.domain.DomainArticle
import com.segunfrancis.newsfeed.domain.DomainSavedArticle

internal fun ArticleDto.toDomainArticle(): DomainArticle {
    return DomainArticle(
        author = author,
        content = content,
        description = description,
        publishedAt = publishedAt,
        id = source?.id,
        name = source?.name.orEmpty(),
        title = title,
        url = url,
        urlToImage = urlToImage,
        category = category,
        fetchedAt = fetchedAt
    )
}

internal fun Article.toDomainArticle(): DomainArticle {
    return DomainArticle(
        author = author,
        content = content,
        description = description,
        publishedAt = publishedAt,
        id = source?.id,
        name = source?.name.orEmpty(),
        title = title,
        url = url,
        urlToImage = urlToImage,
        category = category,
        fetchedAt = fetchedAt
    )
}

internal fun ArticleDto.toLocalArticle(): Article {
    return Article(
        author = author,
        content = content,
        description = description,
        publishedAt = publishedAt,
        source = source?.toLocalSource(),
        title = title,
        url = url,
        urlToImage = urlToImage,
        category = category
    )
}

private fun SourceDto.toLocalSource(): Source {
    return Source(id = id, name = name)
}

internal fun DomainSavedArticle.toEntity() = SavedArticleEntity(
    url = url,
    title = title,
    description = description,
    content = content,
    author = author,
    source = source,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    category = category,
    savedAt = savedAt
)

internal fun SavedArticleEntity.toDomain() = DomainSavedArticle(
    url = url,
    title = title,
    description = description,
    content = content,
    author = author,
    source = source,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    category = category,
    savedAt = savedAt,
)
