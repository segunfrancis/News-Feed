package com.segunfrancis.newsfeed.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.models.HomeArticle

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    Scaffold(topBar = { HomeScreenToolbar(title = R.string.app_name) }) {
        val response = viewModel.homeState.value
        NewsList(newsList = response.articles, modifier = Modifier.padding(it))
    }
}

@Composable
fun HomeScreenToolbar(@StringRes title: Int, modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        backgroundColor = MaterialTheme.colors.onBackground
    ) {
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.caption,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 6.dp),
            color = MaterialTheme.colors.background
        )
    }
}

@Composable
fun NewsItem(modifier: Modifier = Modifier, article: HomeArticle) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = article.author,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
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
@Preview
fun HomeScreenToolbarPreview() {
    HomeScreenToolbar(title = R.string.app_name)
}

@Composable
@Preview
fun NewsItemPreview() {
    NewsItem(article = newsItem)
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

private val newsItem = HomeArticle(
    title = "EU to recommend reinstating restrictions to US travellers",
    author = "Daniel Dale",
    content = "",
    description = "",
    publishedAt = "",
    url = "",
    urlToImage = "",
    onClick = {}
)
