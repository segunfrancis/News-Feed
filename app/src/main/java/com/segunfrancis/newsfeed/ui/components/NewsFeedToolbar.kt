package com.segunfrancis.newsfeed.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.segunfrancis.newsfeed.R
import com.segunfrancis.newsfeed.ui.theme.NewsFeedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedToolbar(
    @StringRes title: Int = R.string.app_name,
    scrollBehavior: TopAppBarScrollBehavior,
    onMenuAction: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = Color(0xFF376A8E),
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = { onSearchClick() },
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        },
        actions = {
            IconButton(
                onClick = { onMenuAction() },
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(uiMode = UI_MODE_NIGHT_NO)
fun NewsFeedToolbarPreview() {
    NewsFeedTheme {
        NewsFeedToolbar(
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            onMenuAction = {},
            onSearchClick = {}
        )
    }
}
