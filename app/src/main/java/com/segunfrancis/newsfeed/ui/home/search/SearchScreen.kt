package com.segunfrancis.newsfeed.ui.home.search

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.SearchItem
import com.segunfrancis.newsfeed.ui.SearchUiState
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@Composable
fun SearchScreen(stateHolder: SearchStateHolder, onItemClick:(String) -> Unit, onMoreClick: (HomeArticle) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    when (val state = stateHolder.uiState) {
        is SearchUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search_error),
                    contentDescription = null,
                    modifier = Modifier.size(68.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.something_went_wrong),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        SearchUiState.Idle -> {}
        SearchUiState.Loading -> {
            LaunchedEffect(Unit) {
                keyboardController?.hide()
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is SearchUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.articles, key = { it.url }) { article ->
                    SearchItem(
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        ),
                        article = article,
                        onClick = { onItemClick(article.url) },
                        onMoreClick = { onMoreClick(article) }
                    )
                }
            }
        }

        is SearchUiState.Empty -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_search_empty),
                    contentDescription = null,
                    modifier = Modifier.size(68.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.not_found),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                val template = stringResource(R.string.not_found_description, "\u0000") // null char as sentinel
                val formatted = stringResource(R.string.not_found_description, state.query)
                val sentinelIndex = template.indexOf('\u0000')

                val annotatedString = buildAnnotatedString {
                    append(formatted)
                    if (sentinelIndex >= 0) {
                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold),
                            start = sentinelIndex,
                            end = sentinelIndex + state.query.length
                        )
                    }
                }
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
