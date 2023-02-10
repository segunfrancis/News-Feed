package com.segunfrancis.newsfeed.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.home.newsItem
import com.segunfrancis.newsfeed.ui.models.HomeArticle
import com.segunfrancis.newsfeed.ui.theme.White500

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsItem(
    modifier: Modifier = Modifier,
    article: HomeArticle,
    onNewsItemClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        backgroundColor = MaterialTheme.colors.background,
        onClick = { onNewsItemClick(article.url) }
    ) {
        Box {
            val context = LocalContext.current
            AsyncImage(
                model = remember(article.urlToImage) {
                    ImageRequest.Builder(context)
                        .data(article.urlToImage)
                        .crossfade(true)
                        .placeholder(R.drawable.image_loader)
                        .error(R.drawable.ic_error)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                },
                contentDescription = "News Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(ContentAlpha.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colors.onBackground
                )

                Text(
                    text = HtmlCompat.fromHtml(article.author, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = White500
                )
            }
        }
    }
}

@Composable
@Preview
fun NewsItemPreview() {
    NewsItem(article = newsItem)
}