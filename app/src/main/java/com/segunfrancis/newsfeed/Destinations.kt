package com.segunfrancis.newsfeed

import androidx.annotation.StringRes
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface Destinations {
    val route: String
}

object Home : Destinations {
    override val route: String = "home_destination"

    @StringRes
    const val title: Int = R.string.app_name
}

object Details : Destinations {
    override val route: String = "details_destination"

    const val detailArg = "url"
    val arguments = listOf(
        navArgument(detailArg) {
            type = NavType.StringType
            defaultValue = ""
        }
    )
}
