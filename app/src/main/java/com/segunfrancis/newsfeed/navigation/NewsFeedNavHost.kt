package com.segunfrancis.newsfeed.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.segunfrancis.newsfeed.Details
import com.segunfrancis.newsfeed.Home
import com.segunfrancis.newsfeed.ui.details.DetailsScreen
import com.segunfrancis.newsfeed.ui.home.HomeScreen

@Composable
fun NewsFeedNavHost(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(route = Home.route) {
            HomeScreen(
                onNewsItemClick = { url ->
                    navController.navigate(route = "${Details.route}?${Details.detailArg}=$url")
                },
                viewModel = hiltViewModel()
            )
        }
        composable(
            route = "${Details.route}?${Details.detailArg}={${Details.detailArg}}",
            arguments = Details.arguments
        ) {
            val url = it.arguments?.getString(Details.detailArg)
            url?.let { it1 -> DetailsScreen(url = it1) }
        }
    }
}
