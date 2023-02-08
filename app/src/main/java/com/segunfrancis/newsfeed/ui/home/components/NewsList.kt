package com.segunfrancis.newsfeed.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segunfrancis.newsfeed.ui.home.newsItem
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@Composable
fun NewsList(modifier: Modifier = Modifier, newsList: List<HomeArticle>, onNewsItemClick: (url: String) -> Unit = { }) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(newsList) { article ->
            NewsItem(article = article) {
                onNewsItemClick(it)
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
    NewsList(newsList = newsList)
}
