package com.segunfrancis.newsfeed.usecase

import com.segunfrancis.newsfeed.repository.NewsFeedRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HomeUseCase @Inject constructor(private val repository: NewsFeedRepository) {

    suspend operator fun invoke(): HomeResponse {
        return try {
            HomeResponse(articles = repository.getNewsArticles().first())
        } catch (t: Throwable) {
            HomeResponse(error = t)
        }
    }
}
