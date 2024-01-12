package com.segunfrancis.newsfeed.ui.home.components

import androidx.annotation.StringRes
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segunfrancis.newsfeed.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedToolbar(
    @StringRes title: Int = R.string.app_name,
    scrollBehavior: TopAppBarScrollBehavior,
    onMenuItemClick: (Int) -> Unit
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                text = stringResource(id = title)
            )
        },
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(
                onClick = { isMenuExpanded.value = true },
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_filter_list),
                        contentDescription = null
                    )
                })
            DropdownMenu(
                expanded = isMenuExpanded.value,
                onDismissRequest = { isMenuExpanded.value = false },
                content = {
                    menuItems.forEachIndexed { index, item ->
                        MenuItem(
                            title = item.title,
                            leadingIcon = item.leadingIcon,
                            onItemClick = {
                                isMenuExpanded.value = false
                                onMenuItemClick(index)
                            })
                    }
                })
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun NewsFeedToolbarPreview() {
    NewsFeedToolbar(
        title = R.string.app_name,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        onMenuItemClick = {}
    )
}
