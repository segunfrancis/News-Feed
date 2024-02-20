package com.segunfrancis.newsfeed.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.data.models.Article
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale

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

val Context.datastore: DataStore<Preferences> by preferencesDataStore("news-feed-datastore")

fun Context.openTab(url: String) {
    val builder = CustomTabsIntent.Builder()
    val colorParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(ContextCompat.getColor(this, R.color.app_color))
        .build()
    builder.setDefaultColorSchemeParams(colorParams)
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(this, Uri.parse(url))
}

fun Article.isRemoved(): Boolean {
    return source?.name.equals("[Removed]", ignoreCase = true) || title.equals(
        "[Removed]",
        ignoreCase = true
    ) || description.equals("[Removed]", ignoreCase = true) || content.equals(
        "[Removed]",
        ignoreCase = true
    ) || url.equals("https://removed.com", ignoreCase = true)
}

fun currentDate(): String {
    val format = SimpleDateFormat("E, dd MMM", Locale.getDefault())
    return format.format(System.currentTimeMillis())
}

fun String?.formatDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date = this?.let { inputFormat.parse(it) }
        val outputFormat = SimpleDateFormat("E, dd MMM", Locale.getDefault())
        date?.let {
            val newDate = outputFormat.format(date)
            if (newDate == currentDate()) {
                "Today"
            } else newDate
        } ?: ""
    } catch (t: Throwable) {
        t.printStackTrace()
        ""
    }
}
