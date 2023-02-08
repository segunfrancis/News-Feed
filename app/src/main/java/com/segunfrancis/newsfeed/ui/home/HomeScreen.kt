package com.segunfrancis.newsfeed.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.home.components.ErrorScreen
import com.segunfrancis.newsfeed.ui.home.components.Header
import com.segunfrancis.newsfeed.ui.home.components.LoadingScreen
import com.segunfrancis.newsfeed.ui.home.components.NewsList
import com.segunfrancis.newsfeed.ui.home.components.TopProgressBar
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onNewsItemClick: (url: String) -> Unit = { }
) {
    val response = viewModel.homeState.value

    if (response.errorMessage != null) {
        if (response.articles.isEmpty()) {
            ErrorScreen(
                errorMessage = response.errorMessage,
                onRetryClick = { viewModel.getHomeData() })
        } else {
            // Show snackbar
            LaunchedEffect(key1 = scaffoldState.snackbarHostState) {
                val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                    message = response.errorMessage,
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Indefinite
                )
                when (snackbarResult) {
                    SnackbarResult.ActionPerformed -> {
                        viewModel.getHomeData()
                    }

                    SnackbarResult.Dismissed -> {}
                }
            }
        }
    }
    if (response.articles.isNotEmpty()) {
        Column(modifier) {
            Header(title = R.string.top_news)
            if (response.isLoading) {
                TopProgressBar()
            }
            NewsList(newsList = response.articles, modifier = modifier) {
                onNewsItemClick(it)
            }
        }
    } else {
        if (response.isLoading) {
            LoadingScreen()
        }
    }
}

val newsItem = HomeArticle(
    title = "EU to recommend reinstating restrictions to US travellers",
    author = "Daniel Dale",
    content = "",
    description = "",
    publishedAt = "",
    url = "",
    urlToImage = "",
    onClick = {}
)
