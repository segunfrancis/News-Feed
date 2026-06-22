package com.segunfrancis.newsfeed.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
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
    isMenuExpanded: Boolean,
    onMenuItemClick: (Int) -> Unit,
    onMenuAction: (Boolean) -> Unit
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
        actions = {
            IconButton(
                onClick = { onMenuAction(true) },
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = null
                    )
                }
            )
            DropdownMenu(
                modifier = Modifier.background(color = Color.White),
                expanded = isMenuExpanded,
                onDismissRequest = { onMenuAction(false) },
                content = {
                    menuItems.forEachIndexed { index, item ->
                        MenuItem(
                            title = item.title,
                            leadingIcon = item.leadingIcon,
                            onItemClick = {
                                onMenuItemClick(index)
                                onMenuAction(false)
                            })
                    }
                })
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
            title = R.string.app_name,
            isMenuExpanded = false,
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            onMenuItemClick = {},
            onMenuAction = {}
        )
    }
}
