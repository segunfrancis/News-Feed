package com.segunfrancis.newsfeed.usecase

import com.segunfrancis.newsfeed.repository.NewsFeedRepository
import com.segunfrancis.newsfeed.repository.RepositoryState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HomeUseCase @Inject constructor(private val repository: NewsFeedRepository) {

    suspend operator fun invoke(): Flow<HomeResponse> {
        return repository.getNewsArticles().map { it.toHomeResponse() }
    }

    private fun RepositoryState.toHomeResponse(): HomeResponse {
        return HomeResponse(articles = articles, networkLoading = networkLoading, error = error)
    }
}
