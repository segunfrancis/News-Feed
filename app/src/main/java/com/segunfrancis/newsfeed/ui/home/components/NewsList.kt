package com.segunfrancis.newsfeed.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.segunfrancis.newsfeed.ui.home.newsItem
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@Composable
fun NewsList(
    modifier: Modifier = Modifier,
    newsList: LazyPagingItems<HomeArticle>?,
    onNewsItemClick: (url: String) -> Unit = { }
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        newsList?.let { article ->
            items(article.itemCount) {
                article[it]?.let { it1 ->
                    NewsItem(article = it1) { url ->
                        onNewsItemClick(url)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun NewsListPreview() {
    val newsList = mutableListOf<HomeArticle>()
    repeat(15) {
        newsList.add(newsItem)
    }
    NewsList(newsList = null)
}
