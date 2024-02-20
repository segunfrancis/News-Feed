package com.segunfrancis.newsfeed.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.segunfrancis.newsfeed.ui.home.components.ErrorScreen
import com.segunfrancis.newsfeed.ui.home.components.Header
import com.segunfrancis.newsfeed.ui.home.components.LoadingScreen
import com.segunfrancis.newsfeed.ui.home.components.NewsFeedToolbar
import com.segunfrancis.newsfeed.ui.home.components.NewsItem
import com.segunfrancis.newsfeed.ui.home.components.TopProgressBar
import com.segunfrancis.newsfeed.ui.home.components.menuItems
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.util.formatDate
import com.segunfrancis.newsfeed.util.openTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val response = viewModel.homeState.value
    val scope = rememberCoroutineScope()
    val optionIndex by viewModel.menuOption.collectAsState(
        initial = 0,
        scope.coroutineContext
    )
    val newsArticles = viewModel.newsArticles?.collectAsLazyPagingItems()
    val context = LocalContext.current

    HomeScreenContent(
        response = response,
        optionIndex = optionIndex,
        newsArticles = newsArticles,
        scope = scope,
        modifier = Modifier
    ) {
        when (it) {
            is HomeScreenUiActions.OnMenuItemClick -> viewModel.setSelectedMenuOption(it.index)
            is HomeScreenUiActions.OnNewsItemClick -> context.openTab(it.url)
            HomeScreenUiActions.OnRetryClick -> viewModel.initRemote()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    response: HomeUiState,
    optionIndex: Int,
    newsArticles: LazyPagingItems<HomeArticle>?,
    scope: CoroutineScope,
    modifier: Modifier,
    onAction: (HomeScreenUiActions) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val lazyListState = rememberLazyListState()
    val visibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                NewsFeedToolbar(
                    scrollBehavior = scrollBehavior,
                    onMenuItemClick = { onAction(HomeScreenUiActions.OnMenuItemClick(it)) })
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedContent(
                targetState = !lazyListState.isScrollInProgress && visibleItemIndex > 3,
                label = "Fab animation container"
            ) {
                if (it) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch { lazyListState.animateScrollToItem(0) }
                        },
                        shape = RoundedCornerShape(50),
                        content = {
                            Image(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Fab"
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        if (response.errorMessage != null) {
            if ((newsArticles?.itemCount ?: 0) <= 0) {
                ErrorScreen(
                    errorMessage = response.errorMessage,
                    onRetryClick = { onAction(HomeScreenUiActions.OnRetryClick) })
            } else {
                LaunchedEffect(key1 = snackbarHostState) {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = response.errorMessage,
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Indefinite
                    )
                    when (snackbarResult) {
                        SnackbarResult.ActionPerformed -> {
                            onAction(HomeScreenUiActions.OnRetryClick)
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
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = lazyListState
                ) {
                    newsArticles?.let { article ->
                        val groupedArticles = article.itemSnapshotList.groupBy { it?.publishedAt.formatDate() }
                        groupedArticles.forEach { (date, article) ->
                            stickyHeader {
                                Text(
                                    text = date,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }

                            items(article) { homeArticle ->
                                homeArticle?.let {
                                    NewsItem(article = it) { url ->
                                        onAction(HomeScreenUiActions.OnNewsItemClick(url))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (response.isLoading) {
                LoadingScreen()
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        response = HomeUiState(isLoading = true, errorMessage = "Something went wrong"),
        optionIndex = 2,
        newsArticles = null,
        scope = rememberCoroutineScope(),
        modifier = Modifier
    ) {}
}

sealed interface HomeScreenUiActions {
    object OnRetryClick : HomeScreenUiActions
    data class OnMenuItemClick(val index: Int) : HomeScreenUiActions
    data class OnNewsItemClick(val url: String) : HomeScreenUiActions
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
