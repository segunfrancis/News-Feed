package com.segunfrancis.newsfeed.util

import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.handleThrowable(): String {
    Timber.e(this)
    return when {
        this is UnknownHostException || this is ConnectException -> "Please check your internet connection and try again"
        this is HttpException && this.code() == 401 -> "Your API key is invalid or incorrect. Check your key, or " +
                "go to https://newsapi.org to create a free API key."
        this is HttpException && this.code() == 403 -> "You are unauthorized to make this action"
        this is HttpException && this.code() in 500..599 -> "Something went wrong.\nIt is not you, it is us"
        this is HttpException -> "Something went wrong.\n" +
                "It is not you, it is us. Try again"

        this is SocketTimeoutException -> "Please check your network connection.\nMake sure you are connected to a good network"
        else -> "Something went wrong"
    }
}
