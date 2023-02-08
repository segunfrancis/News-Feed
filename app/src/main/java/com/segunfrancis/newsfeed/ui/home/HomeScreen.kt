package com.segunfrancis.newsfeed.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    val response = viewModel.homeState.value

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { NewsFeedToolbar(title = R.string.app_name) },
        backgroundColor = MaterialTheme.colors.onBackground
    ) {
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
                if (response.isLoading) { TopProgressBar() }
                NewsList(newsList = response.articles, modifier = Modifier.padding(it))
            }
        } else {
            if (response.isLoading) {
                LoadingScreen()
            }
        }
    }
}

@Composable
fun NewsList(modifier: Modifier = Modifier, newsList: List<HomeArticle>) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(newsList) { article ->
            NewsItem(article = article)
        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier, @StringRes title: Int) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.onBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colors.background
        )
    }
}

@Composable
@Preview
fun NewsListPreview() {
    val newsList = mutableListOf<HomeArticle>()
    repeat(15) {
        newsList.add(newsItem)
    }
    NewsList(newsList = newsList)
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

@Composable
@Preview
fun HeaderPreview() {
    Header(title = R.string.top_news)
}
