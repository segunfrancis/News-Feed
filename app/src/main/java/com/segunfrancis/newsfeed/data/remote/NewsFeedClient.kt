package com.segunfrancis.newsfeed.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.segunfrancis.newsfeed.util.AppConstants.BASE_URL
import com.segunfrancis.newsfeed.util.AppConstants.NETWORK_TIMEOUT
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NewsFeedClient {

    private fun provideGson(): Gson {
        return GsonBuilder().setLenient().create()
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY }
    }

    private fun provideClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .addInterceptor(provideLoggingInterceptor())
            .callTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    fun getApi(): NewsFeedApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(provideGson()))
            .client(provideClient())
            .build()
            .create(NewsFeedApi::class.java)
    }
}
