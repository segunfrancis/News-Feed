package com.segunfrancis.newsfeed.usecase

import com.segunfrancis.newsfeed.repository.NewsFeedRepository
import javax.inject.Inject

class HomeUseCase @Inject constructor(private val repository: NewsFeedRepository) {

    suspend operator fun invoke(): HomeResponse {
        return try {
            HomeResponse(articlesFlow = repository.getNewsArticles())
        } catch (t: Throwable) {
            HomeResponse(error = t)
        }
    }
}
