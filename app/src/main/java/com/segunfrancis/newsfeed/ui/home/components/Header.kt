package com.segunfrancis.newsfeed.ui.home.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segunfrancis.newsfeed.R

@Composable
fun Header(modifier: Modifier = Modifier, @StringRes title: Int) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.onBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colors.background
        )
    }
}

@Composable
@Preview
fun HeaderPreview() {
    Header(title = R.string.top_news)
}
