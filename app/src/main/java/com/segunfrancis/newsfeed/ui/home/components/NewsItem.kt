package com.segunfrancis.newsfeed.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.segunfrancis.newsfeed.util.formatDate

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
                        .error(R.drawable.image_error)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                },
                contentDescription = "News Image",
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.4F))
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = HtmlCompat.fromHtml(article.author, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            .toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .weight(1F),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
                    )

                    Text(
                        text = article.publishedAt.formatDate(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
@Preview
fun NewsItemPreview() {
    NewsItem(article = newsItem)
}
