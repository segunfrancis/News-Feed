package com.segunfrancis.newsfeed.data.remote

import com.segunfrancis.newsfeed.data.models.NewsBaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsFeedApi {

    @GET("top-headlines")
    suspend fun getNews(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = ""
    ): NewsBaseResponse
}
