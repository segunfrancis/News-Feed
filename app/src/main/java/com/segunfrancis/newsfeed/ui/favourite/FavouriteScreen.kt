package com.segunfrancis.newsfeed.ui.favourite

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.components.CommonOptionBottomSheet
import com.segunfrancis.newsfeed.util.openTab
import com.segunfrancis.newsfeed.util.shareUrl
import com.segunfrancis.newsfeed.util.toRelativeTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(snackbarHostState: SnackbarHostState, lazyListState: LazyListState) {
    val viewModel = hiltViewModel<FavouriteViewModel>()
    val favourites by viewModel.favouriteArticles.collectAsStateWithLifecycle()
    val selectedArticle by viewModel.selectedArticle.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val resource = LocalResources.current
    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is FavouriteActions.OnError -> {
                    snackbarHostState.showSnackbar(
                        message = action.message,
                        withDismissAction = true
                    )
                }

                FavouriteActions.OnRemoveBookmark -> {
                    snackbarHostState.showSnackbar(resource.getString(R.string.removed_from_save))
                }
            }
        }
    }

    selectedArticle?.let {
        CommonOptionBottomSheet(
            isBookmarked = true,
            bottomSheetState = bottomSheetState,
            onDismissRequest = { viewModel.updateSelectedArticle(null) },
            onBookmarkClick = {
                viewModel.removeBookmark()
                viewModel.updateSelectedArticle(null)
            },
            onShareClick = {
                context.shareUrl(it.url)
                viewModel.updateSelectedArticle(null)
            }
        )
    }
    FavouriteContent(
        favourites = favourites,
        lazyListState = lazyListState,
        onClick = { url -> context.openTab(url) },
        onMoreClick = { viewModel.updateSelectedArticle(it) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavouriteContent(
    favourites: List<SavedArticle>,
    lazyListState: LazyListState,
    onClick: (String) -> Unit,
    onMoreClick: (SavedArticle) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favourites, key = { it.url }) { article ->
                FavouriteItem(
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                        placementSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    ),
                    article = article,
                    onClick = { onClick(article.url) },
                    onMoreClick = { onMoreClick(article) }
                )
            }
        }
    }
}

@Preview
@Composable
fun FavouriteItem(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {},
    article: SavedArticle = savedArticle,
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
                modifier = Modifier.constrainAs(source) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
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
            model = article.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.image_error),
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .constrainAs(image) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
        )
    }
}

@Preview
@Composable
fun FavouriteContentPreview() {
    FavouriteContent(
        favourites = listOf(
            savedArticle,
            savedArticle.copy(url = "www.savedarticle2.com", source = null)
        ),
        lazyListState = rememberLazyListState(),
        onClick = {},
        onMoreClick = {}
    )
}

val savedArticle = SavedArticle(
    title = "EU to recommend reinstating restrictions to US travellers",
    author = "Daniel Dale",
    content = "",
    description = "",
    publishedAt = "2026-06-18T00:08:24Z",
    url = "www.savedarticle.com",
    imageUrl = "",
    savedAt = 900L,
    source = "CNN News",
    category = "Entertainment",
    relativePublishedTime = "2026-06-18T00:08:24Z".toRelativeTimeString()
)
