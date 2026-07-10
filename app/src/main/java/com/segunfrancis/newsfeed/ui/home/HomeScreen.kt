package com.segunfrancis.newsfeed.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.components.CommonOptionBottomSheet
import com.segunfrancis.newsfeed.ui.components.ErrorScreen
import com.segunfrancis.newsfeed.ui.components.LoadingScreen
import com.segunfrancis.newsfeed.ui.components.SingleMenuItem
import com.segunfrancis.newsfeed.ui.components.StickyHeader
import com.segunfrancis.newsfeed.ui.components.menuItems
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.util.formatDate
import com.segunfrancis.newsfeed.util.getOrNull
import com.segunfrancis.newsfeed.util.handleThrowable
import com.segunfrancis.newsfeed.util.openTab
import com.segunfrancis.newsfeed.util.shareUrl
import com.segunfrancis.newsfeed.util.toRelativeTimeString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resource = LocalResources.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedArticle by viewModel.selectedArticle.collectAsState()
    val isBookmarked by viewModel.isSelectedArticleBookmarked.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.bookmarkAction.collect { action ->
            when (action) {
                BookmarkActions.OnAddBookmark -> {
                    snackbarHostState.showSnackbar(
                        message = resource.getString(R.string.added_to_save),
                        duration = SnackbarDuration.Short
                    )
                }
                is BookmarkActions.OnError -> {
                    snackbarHostState.showSnackbar(
                        message = action.message,
                        duration = SnackbarDuration.Short
                    )
                }
                BookmarkActions.OnRemoveBookmark -> {
                    snackbarHostState.showSnackbar(
                        message = resource.getString(R.string.removed_from_save),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    HomeScreenContent(
        modifier = Modifier,
        snackbarHostState = snackbarHostState,
        articlesForCategory = viewModel::articlesForCategory,
        pagingEventsFor = { viewModel.pagingEventsFor(it) },
        onAction = {
            when (it) {
                is HomeScreenUiActions.OnNewsItemClick -> context.openTab(it.url)
                is HomeScreenUiActions.OnRetryClick -> viewModel.retry(it.category)
                is HomeScreenUiActions.OnRefreshAction -> viewModel.refresh(it.category)
                is HomeScreenUiActions.OnMoreClick -> {
                    viewModel.setSelectedArticle(it.article)
                    viewModel.setSelectedCategory(it.category)
                }
            }
        }
    )

    selectedArticle?.let {
        CommonOptionBottomSheet(
            isBookmarked = isBookmarked,
            bottomSheetState = bottomSheetState,
            onDismissRequest = { viewModel.setSelectedArticle(null) },
            onBookmarkClick = {
                viewModel.toggleBookmark()
                viewModel.setSelectedArticle(null)
            },
            onShareClick = {
                context.shareUrl(it.url)
                viewModel.setSelectedArticle(null)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onAction: (HomeScreenUiActions) -> Unit,
    articlesForCategory: (String) -> Flow<PagingData<HomeArticle>>,
    pagingEventsFor: (String) -> Flow<PagingEvent>
) {
    val pagerState = rememberPagerState(pageCount = { menuItems.size })
    val scope = rememberCoroutineScope()

    // Hoist one LazyListState per tab here so the tab row and each page share
    // the same state object. menuItems.size is a compile-time constant so the
    // number of rememberLazyListState() calls is always identical — stable for Compose.
    val lazyListStates = menuItems.map { rememberLazyListState() }

    Column(modifier) {
        NewsScrollableTabRow(
            tabs = menuItems,
            pagerState = pagerState,
            onTabSelected = { index ->
                scope.launch {
                    if (index == pagerState.settledPage) {
                        // Already on this tab — scroll its list back to the top.
                        // animateScrollToItem respects the list's own scroll
                        // animation and will no-op cleanly if already at item 0.
                        lazyListStates[index].animateScrollToItem(0)
                    } else {
                        pagerState.animateScrollToPage(
                            page = index,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            ),
                        )
                    }
                }
            },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { menuItems[it].title },
        ) { page ->
            val tab = menuItems[page]
            val category = tab.queryParam
            val isCurrentPage = pagerState.settledPage == page

            // Each page gets its own independent paging items.
            // collectAsLazyPagingItems() is stable across recompositions because
            // the underlying flow reference never changes (pre-built Map in VM).
            val articles = articlesForCategory(category).collectAsLazyPagingItems()

            // FIX 1: wire paging events for THIS category only.
            // LaunchedEffect(articles) re-registers if articles is recreated
            // (e.g. after process death + restore).
            LaunchedEffect(articles) {
                pagingEventsFor(category).collect { event ->
                    when (event) {
                        PagingEvent.Refresh -> articles.refresh()
                        PagingEvent.Retry -> articles.retry()
                    }
                }
            }

            // FIX 2: derive error/loading state per page, not once for the whole screen.
            val mediatorRefreshState = articles.loadState.mediator?.refresh
            val isInitialLoad = mediatorRefreshState is LoadState.Loading && articles.itemCount == 0
            val isCriticalError = mediatorRefreshState is LoadState.Error && articles.itemCount == 0
            val hasBackgroundError =
                mediatorRefreshState is LoadState.Error && articles.itemCount > 0

            if (hasBackgroundError && isCurrentPage) {
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val result = snackbarHostState.showSnackbar(
                        message = mediatorRefreshState.error.handleThrowable(),
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onAction(HomeScreenUiActions.OnRetryClick(category))
                    }
                }
            }

            when {
                isInitialLoad -> LoadingScreen()

                isCriticalError -> ErrorScreen(
                    errorMessage = mediatorRefreshState.error.handleThrowable(),
                    onRetryClick = { onAction(HomeScreenUiActions.OnRetryClick(category)) },
                )

                else -> NewsTabContent(
                    tab = tab,
                    articleItems = articles,
                    lazyListState = lazyListStates[page],
                    onItemClick = { onAction(HomeScreenUiActions.OnNewsItemClick(it)) },
                    onRefresh = { onAction(HomeScreenUiActions.OnRefreshAction(category)) },
                    onMoreClick = {
                        onAction(
                            HomeScreenUiActions.OnMoreClick(
                                category = category,
                                article = it
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewsScrollableTabRow(
    tabs: List<SingleMenuItem>,
    pagerState: PagerState,
    onTabSelected: (Int) -> Unit,
) {
    val selectedIndex = pagerState.currentPage

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
        indicator = {
            PagerTabIndicator(
                pagerState = pagerState,
                indicatorColor = MaterialTheme.colorScheme.secondary
            )
        },
        divider = {
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                ),
                animationSpec = tween(200),
                label = "tab_label_color_$index",
            )
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                modifier = Modifier.height(44.dp),
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            ) {
                Text(
                    text = tab.title,
                    color = labelColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabIndicatorScope.PagerTabIndicator(
    pagerState: PagerState,
    indicatorColor: Color,
    modifier: Modifier = Modifier,
    indicatorHeight: Dp = 3.dp,
) {
    Box(
        modifier = modifier
            .tabIndicatorLayout { measurable, constraints, tabPositions ->
                if (tabPositions.isEmpty()) {
                    return@tabIndicatorLayout layout(0, 0) {}
                }

                // 1. Calculate horizontal interpolation matching user swipes
                val currentPage = minOf(pagerState.currentPage, tabPositions.lastIndex)
                val fraction = pagerState.currentPageOffsetFraction

                val targetPage = (currentPage + if (fraction > 0) 1 else -1)
                    .coerceIn(tabPositions.indices)

                val currentTab = tabPositions[currentPage]
                val targetTab = tabPositions[targetPage]
                val absFraction = abs(fraction)

                val indicatorWidth = lerp(currentTab.width, targetTab.width, absFraction)
                val indicatorOffset = lerp(currentTab.left, targetTab.left, absFraction)

                // 2. Measure the box matching the precise target tab width
                val widthPx = indicatorWidth.roundToPx()
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = widthPx,
                        maxWidth = widthPx,
                        minHeight = 0,
                        maxHeight = constraints.maxHeight
                    )
                )

                // 3. Set layout width to widthPx so it scrolls natively with the tabs track
                layout(widthPx, constraints.maxHeight) {
                    val yBottomOffset = constraints.maxHeight - placeable.height
                    // 4. Place relative using the offset calculated from the track start
                    placeable.placeRelative(indicatorOffset.roundToPx(), yBottomOffset)
                }
            }
            // 5. Perfect alignment styling
            .height(indicatorHeight)
            .padding(horizontal = 12.dp) // Adjust this value to perfectly match your text's bounds
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .background(indicatorColor)
    )
}

@Composable
private fun NewsTabContent(
    tab: SingleMenuItem,
    articleItems: LazyPagingItems<HomeArticle>,
    lazyListState: LazyListState,
    onItemClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onMoreClick: (HomeArticle) -> Unit
) {
    val mediatorRefreshState = articleItems.loadState.mediator?.refresh
    // True when refreshing but cached data already exists (pull-to-refresh feel)
    val isRefreshing = mediatorRefreshState is LoadState.Loading && articleItems.itemCount > 0

    // FIX 4: groupedArticles computed in remember, NOT inside the lazy
    // layout scope. itemSnapshotList inside lazy measurement triggers
    // extra layout passes on every frame.
    //
    // Keyed on itemSnapshotList so it only recomputes when paging loads
    // new items — not on every recomposition.
    val snapshot = articleItems.itemSnapshotList
    val groupedArticles = remember(snapshot) {
        snapshot.items.drop(1).groupBy { it.formattedPublishedTime }
    }

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { onRefresh() }) {
        if (articleItems.itemCount > 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                state = lazyListState
            ) {
                // Prominent top story
                articleItems.getOrNull(0)?.let {
                    item {
                        HeroArticleCard(
                            article = it,
                            onClick = onItemClick,
                            onMoreClick = { onMoreClick(it) })
                    }
                }

                // Section divider
                item {
                    SectionLabel()
                }

                groupedArticles.forEach { (date, articles) ->
                    stickyHeader(key = "header_${tab.title}_$date") {
                        StickyHeader(date)
                    }
                    items(
                        items = articles,
                        key = { "${tab.title}_${it.url}" },    // url is more stable than title
                    ) { article ->
                        CompactArticleCard(
                            article = article,
                            onClick = onItemClick,
                            onMoreClick = { onMoreClick(article) })
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }

                // Bottom breathing room
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Preview
@Composable
private fun HeroArticleCard(
    article: HomeArticle = newsItem,
    onClick: (String) -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(article.url) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            AsyncImage(
                model = article.urlToImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                error = painterResource(R.drawable.image_error)
            )
            Spacer(Modifier.height(6.dp))

            article.source?.let {
                Text(
                    text = article.source,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(2.dp))

            // Headline
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 22.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Meta row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1F)) {
                    article.author?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = article.relativePublishedTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
                Row {
                    IconButton(onClick = { onMoreClick() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = "Bookmark",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CompactArticleCard(
    article: HomeArticle = newsItem,
    onClick: (String) -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(article.url) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)),
        )

        Column(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = article.urlToImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                error = painterResource(R.drawable.image_error)
            )
            Spacer(Modifier.height(8.dp))
            article.source?.let {
                Text(
                    text = article.source,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    article.author?.let {
                        Text(
                            text = article.author,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1F, fill = false),
                        )
                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                    Text(
                        text = article.relativePublishedTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
                IconButton(onClick = { onMoreClick() }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "More options",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
        )
        Text(
            text = "Latest Stories".uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

sealed interface HomeScreenUiActions {
    data class OnRetryClick(val category: String) : HomeScreenUiActions
    data class OnRefreshAction(val category: String) : HomeScreenUiActions
    data class OnNewsItemClick(val url: String) : HomeScreenUiActions
    data class OnMoreClick(val category: String, val article: HomeArticle) : HomeScreenUiActions
}

val newsItem = HomeArticle(
    title = "EU to recommend reinstating restrictions to US travellers",
    author = "Daniel Dale",
    content = "",
    description = "",
    publishedAt = "2026-06-18T00:08:24Z",
    url = "",
    urlToImage = "",
    relativePublishedTime = "2026-06-18T00:08:24Z".toRelativeTimeString(),
    source = "BBC News",
    formattedPublishedTime = "2026-06-18T00:08:24Z".formatDate()
)
