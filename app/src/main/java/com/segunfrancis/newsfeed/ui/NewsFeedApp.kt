package com.segunfrancis.newsfeed.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.segunfrancis.newsfeed.Home
import com.segunfrancis.newsfeed.navigation.NewsFeedNavHost
import com.segunfrancis.newsfeed.ui.home.components.NewsFeedToolbar

@Composable
fun NewsFeedApp(modifier: Modifier = Modifier, navController: NavHostController, scaffoldState: ScaffoldState) {

    Scaffold(
        modifier = modifier,
        topBar = {
            NewsFeedToolbar(title = Home.title)
        },
        scaffoldState = scaffoldState
    ) { innerPadding ->
        NewsFeedNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            scaffoldState = scaffoldState
        )
    }
}
