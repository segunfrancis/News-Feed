package com.segunfrancis.newsfeed.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.segunfrancis.newsfeed.Home
import com.segunfrancis.newsfeed.navigation.NewsFeedNavHost
import com.segunfrancis.newsfeed.ui.home.components.NewsFeedToolbar

@Composable
fun NewsFeedApp(modifier: Modifier = Modifier, navController: NavHostController) {
    Scaffold(
        modifier = modifier,
        topBar = {
            navController.currentDestination?.let {
                if (it.route == Home.route) {
                    NewsFeedToolbar(title = Home.title)
                }
            }
        }
    ) { innerPadding ->
        NewsFeedNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
