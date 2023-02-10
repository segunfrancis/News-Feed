package com.segunfrancis.newsfeed.ui.home.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.theme.Black700

@Composable
fun NewsFeedToolbar(@StringRes title: Int, modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        backgroundColor = Black700
    ) {
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.caption,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 6.dp),
            color = MaterialTheme.colors.onBackground
        )
    }
}

@Composable
@Preview
fun NewsFeedToolbarPreview() {
    NewsFeedToolbar(title = R.string.app_name)
}
