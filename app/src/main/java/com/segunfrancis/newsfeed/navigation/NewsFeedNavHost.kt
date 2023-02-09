package com.segunfrancis.newsfeed.navigation

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.segunfrancis.newsfeed.Home
import com.segunfrancis.newsfeed.ui.home.HomeScreen

@Composable
fun NewsFeedNavHost(modifier: Modifier = Modifier, navController: NavHostController, scaffoldState: ScaffoldState) {
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(route = Home.route) {
            val context = LocalContext.current
            HomeScreen(
                onNewsItemClick = { url ->
                    openTab(context = context, url = url)
                },
                viewModel = hiltViewModel(),
                scaffoldState = scaffoldState
            )

            /* Add other destinations below */

        }
    }
}

fun openTab(context: Context, url: String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}
