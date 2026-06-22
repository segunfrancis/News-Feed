package com.segunfrancis.newsfeed.data.remote

import com.segunfrancis.newsfeed.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsFeedApi {

    @GET("top-headlines")
    suspend fun getNews(
        @Query("country") country: String = "us",
        @Query("category") category: String,
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsBaseResponseDto
}
