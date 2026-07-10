package com.segunfrancis.newsfeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.segunfrancis.newsfeed.Favourite
import com.segunfrancis.newsfeed.Home
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.Settings
import com.segunfrancis.newsfeed.TOP_LEVEL_NAV_ITEMS
import com.segunfrancis.newsfeed.ui.components.CommonOptionBottomSheet
import com.segunfrancis.newsfeed.ui.components.NewsFeedToolbar
import com.segunfrancis.newsfeed.ui.favourite.FavouriteScreen
import com.segunfrancis.newsfeed.ui.home.HomeScreen
import com.segunfrancis.newsfeed.ui.home.search.SearchScreen
import com.segunfrancis.newsfeed.ui.home.search.rememberSearchStateHolder
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.ui.settings.SettingsScreen
import com.segunfrancis.newsfeed.ui.theme.NewsFeedTheme
import com.segunfrancis.newsfeed.util.openTab
import com.segunfrancis.newsfeed.util.shareUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedTheme {
                val mainViewModel = hiltViewModel<MainViewModel>()
                val navController = rememberNavController()
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                val snackbarHostState = remember { SnackbarHostState() }
                val scrollBehavior =
                    TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
                val lazyListState = rememberLazyListState()
                val visibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
                val scope = rememberCoroutineScope()

                NavigationSuiteScaffold(
                    layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                        currentWindowAdaptiveInfo()
                    ),
                    navigationSuiteItems = {
                        TOP_LEVEL_NAV_ITEMS.forEach { (destinations, item) ->
                            val selected = destinations.route == currentDestination
                            item(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destinations.route) {
                                        restoreState = true
                                        launchSingleTop = true
                                        popUpTo(Home.route) {
                                            saveState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(if (selected) item.selectedIcon else item.unselectedIcon),
                                        contentDescription = stringResource(item.titleTextId)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(item.iconTextId))
                                }
                            )
                        }
                    }
                ) {
                    val searchBarState = rememberSearchBarState()
                    val textFieldState = rememberTextFieldState()
                    val inputField = @Composable {
                        SearchBarDefaults.InputField(
                            textFieldState = textFieldState,
                            searchBarState = searchBarState,
                            onSearch = { mainViewModel.submitSearchQuery(it) },
                            placeholder = {
                                Text(
                                    modifier = Modifier.clearAndSetSemantics {},
                                    text = "Search for topics"
                                )
                            },
                            leadingIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        searchBarState.animateToCollapsed()
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_back),
                                        contentDescription = null
                                    )
                                }
                            },
                            trailingIcon = {
                                if (textFieldState.text.isNotEmpty()) {
                                    IconButton(onClick = {
                                        textFieldState.edit {
                                            delete(0, textFieldState.text.length)
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_close),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        )
                    }
                    ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                        val searchSnackbarHostState = remember { SnackbarHostState() }
                        val searchStateHolder = rememberSearchStateHolder(
                            onSearch = mainViewModel.searchOperation,
                        )
                        val pendingQuery by mainViewModel.pendingSearchQuery.collectAsStateWithLifecycle()
                        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        val selectedArticle by mainViewModel.selectedArticle.collectAsState()
                        val isBookmarked by mainViewModel.isSelectedArticleBookmarked.collectAsState()
                        LaunchedEffect(pendingQuery) {
                            if (pendingQuery.isNotBlank()) {
                                searchStateHolder.search(pendingQuery)
                            }
                        }
                        LaunchedEffect(Unit) {
                            mainViewModel.bookmarkAction.collect { action ->
                                when (action) {
                                    SearchBookmarkActions.OnAddBookmark -> {
                                        searchSnackbarHostState.showSnackbar(
                                            message = getString(R.string.added_to_save),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    is SearchBookmarkActions.OnError -> {
                                        searchSnackbarHostState.showSnackbar(
                                            message = action.message,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    SearchBookmarkActions.OnRemoveBookmark -> {
                                        searchSnackbarHostState.showSnackbar(
                                            message = getString(R.string.removed_from_save),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        }
                        DisposableEffect(Unit) {
                            onDispose {
                                mainViewModel.clearPendingQuery()  // reset the bridge
                                textFieldState.edit { delete(0, textFieldState.text.length) }
                                // searchStateHolder is GC'd automatically — no explicit reset needed
                            }
                        }

                        selectedArticle?.let {
                            CommonOptionBottomSheet(
                                isBookmarked = isBookmarked,
                                bottomSheetState = bottomSheetState,
                                onDismissRequest = { mainViewModel.setSelectedArticle(null) },
                                onBookmarkClick = {
                                    mainViewModel.toggleBookmark()
                                    mainViewModel.setSelectedArticle(null)
                                },
                                onShareClick = {
                                    shareUrl(it.url)
                                    mainViewModel.setSelectedArticle(null)
                                }
                            )
                        }
                        Box {
                            SearchScreen(
                                stateHolder = searchStateHolder,
                                onItemClick = { openTab(it) },
                                onMoreClick = { mainViewModel.setSelectedArticle(it) }
                            )
                            SnackbarHost(
                                hostState = searchSnackbarHostState,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                NewsFeedToolbar(
                                    title = if (currentDestination == Favourite.route) Favourite.title else if (currentDestination == Settings.route) Settings.title else Home.title,
                                    scrollBehavior = scrollBehavior,
                                    onMenuAction = { navController.navigate(Settings.route) },
                                    onSearchClick = {
                                        scope.launch {
                                            searchBarState.animateToExpanded()
                                        }
                                    }
                                )
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
                        NavHost(
                            navController = navController,
                            startDestination = Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(route = Home.route) {
                                HomeScreen(snackbarHostState = snackbarHostState)
                            }
                            composable(route = Favourite.route) {
                                FavouriteScreen(
                                    snackbarHostState = snackbarHostState,
                                    lazyListState = lazyListState
                                )
                            }
                            composable(route = Settings.route) {
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItem(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {},
    article: HomeArticle,
    onMoreClick: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(article.url) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        val (source, title, publishedAt, more, image) = createRefs()
        article.source?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(source) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(image.start, margin = 6.dp)
                    width = Dimension.fillToConstraints
                }
            )
        }
        Text(
            text = article.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 22.sp,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(source.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(image.start, margin = 6.dp)
                width = Dimension.fillToConstraints
            }
        )
        Text(
            text = article.relativePublishedTime,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.constrainAs(publishedAt) {
                top.linkTo(more.top)
                bottom.linkTo(more.bottom)
                start.linkTo(parent.start, margin = 4.dp)
            }
        )
        IconButton(
            onClick = { onMoreClick() }, modifier = Modifier
                .size(32.dp)
                .constrainAs(more) {
                    end.linkTo(parent.end)
                    top.linkTo(image.bottom, margin = 4.dp)
                }) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(18.dp),
            )
        }

        AsyncImage(
            model = article.urlToImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.image_error),
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .constrainAs(image) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
        )
    }
}
