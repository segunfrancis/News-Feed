package com.segunfrancis.newsfeed.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.segunfrancis.newsfeed.ui.home.components.ErrorScreen
import com.segunfrancis.newsfeed.ui.home.components.Header
import com.segunfrancis.newsfeed.ui.home.components.LoadingScreen
import com.segunfrancis.newsfeed.ui.home.components.NewsFeedToolbar
import com.segunfrancis.newsfeed.ui.home.components.NewsList
import com.segunfrancis.newsfeed.ui.home.components.TopProgressBar
import com.segunfrancis.newsfeed.ui.home.components.menuItems
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNewsItemClick: (url: String) -> Unit = { }
) {
    val response = viewModel.homeState.value
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val optionIndex by viewModel.menuOption.collectAsState(
        initial = 0,
        scope.coroutineContext
    )
    val newsArticles = viewModel.newsArticles?.collectAsLazyPagingItems()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                NewsFeedToolbar(
                    scrollBehavior = scrollBehavior,
                    onMenuItemClick = { viewModel.setSelectedMenuOption(it) })
            }
        }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->

        if (response.errorMessage != null) {
            if ((newsArticles?.itemCount ?: 0) <= 0) {
                ErrorScreen(
                    errorMessage = response.errorMessage,
                    onRetryClick = { viewModel.initRemote() })
            } else {
                LaunchedEffect(key1 = snackbarHostState) {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = response.errorMessage,
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Indefinite
                    )
                    when (snackbarResult) {
                        SnackbarResult.ActionPerformed -> {
                            viewModel.initRemote()
                        }

                        SnackbarResult.Dismissed -> {}
                    }
                }
            }
        }
        if ((newsArticles?.itemCount ?: 0) > 0) {
            Column(modifier.padding(innerPadding)) {
                Header(title = menuItems[optionIndex].title)
                if (response.isLoading) {
                    TopProgressBar()
                }
                NewsList(newsList = newsArticles, modifier = modifier) {
                    onNewsItemClick(it)
                }
            }
        } else {
            if (response.isLoading) {
                LoadingScreen()
            }
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
