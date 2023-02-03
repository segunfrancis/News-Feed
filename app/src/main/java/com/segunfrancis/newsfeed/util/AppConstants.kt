package com.segunfrancis.newsfeed.util

import com.segunfrancis.newsfeed.BuildConfig

object AppConstants {

    const val BASE_URL: String = "https://newsapi.org/v2/top-headlines/"
    const val DATABASE_NAME: String = BuildConfig.APPLICATION_ID.plus("_database")
    const val NETWORK_TIMEOUT: Long = 30L
}
