package com.segunfrancis.newsfeed

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface Destinations {
    val route: String
}

object Home : Destinations {
    override val route: String = "home_destination"

    @StringRes
    val title: Int = R.string.app_name
}

object Favourite : Destinations {
    override val route: String
        get() = "favourite_destination"

    @StringRes
    val title: Int = R.string.saved_news
}

object Settings : Destinations {
    override val route: String = "settings_destination"

    @StringRes
    val title: Int = R.string.settings
}

data class TopLevelNavItem(
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unselectedIcon: Int,
    @param:StringRes val iconTextId: Int,
    @param:StringRes val titleTextId: Int,
)

val HOME_NAV_ITEM = TopLevelNavItem(
    selectedIcon = R.drawable.ic_home_filled,
    unselectedIcon = R.drawable.ic_home_outline,
    iconTextId = R.string.home,
    titleTextId = R.string.app_name
)

val FAVOURITE_NAV_ITEM = TopLevelNavItem(
    selectedIcon = R.drawable.ic_bookmark_filled,
    unselectedIcon = R.drawable.ic_bookmark_outline,
    iconTextId = R.string.saved,
    titleTextId = R.string.saved_news
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    Home to HOME_NAV_ITEM,
    Favourite to FAVOURITE_NAV_ITEM
)
