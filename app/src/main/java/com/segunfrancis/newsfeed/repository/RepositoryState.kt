package com.segunfrancis.newsfeed.repository

import androidx.paging.PagingSource
import com.segunfrancis.newsfeed.data.models.Article

data class RepositoryState(
    val articles: PagingSource<Int, Article>? = null,
    val networkLoading: Boolean = false,
    val error: Throwable? = null
)

data class RepositoryRemoteState(
    val networkLoading: Boolean = false,
    val error: Throwable? = null
)
