package com.segunfrancis.newsfeed

import androidx.annotation.StringRes

interface Destinations {
    val route: String
}

object Home : Destinations {
    override val route: String = "home_destination"

    @StringRes
    const val title: Int = R.string.app_name
}
