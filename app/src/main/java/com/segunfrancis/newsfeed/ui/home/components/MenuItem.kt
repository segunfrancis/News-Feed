package com.segunfrancis.newsfeed.ui.home.components

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.segunfrancis.newsfeed.R

@Composable
fun MenuItem(
    title: String,
    @DrawableRes leadingIcon: Int,
    isSelected: Boolean = false,
    onItemClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = title, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(painter = painterResource(leadingIcon), contentDescription = null) },
        trailingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        onClick = { onItemClick() }
    )
}

data class SingleMenuItem(
    val title: String,
    @DrawableRes val leadingIcon: Int,
    val isSelected: Boolean,
    val queryParam: String
)

val menuItems = listOf(
    SingleMenuItem(
        title = "Top News",
        leadingIcon = R.drawable.ic_general,
        isSelected = true,
        queryParam = "general"
    ),
    SingleMenuItem(
        title = "Business",
        leadingIcon = R.drawable.ic_business_center,
        isSelected = false,
        queryParam = "business"
    ),
    SingleMenuItem(
        title = "Entertainment",
        leadingIcon = R.drawable.ic_entertainment,
        isSelected = false,
        queryParam = "entertainment"
    ),
    SingleMenuItem(
        title = "Health",
        leadingIcon = R.drawable.ic_health_and_safety,
        isSelected = false,
        queryParam = "health"
    ),
    SingleMenuItem(
        title = "Science",
        leadingIcon = R.drawable.ic_science,
        isSelected = false,
        queryParam = "science"
    ),
    SingleMenuItem(
        title = "Technology",
        leadingIcon = R.drawable.ic_technology,
        isSelected = false,
        queryParam = "technology"
    ),
    SingleMenuItem(
        title = "Sports",
        leadingIcon = R.drawable.ic_sports,
        isSelected = false,
        queryParam = "sports"
    )
)

@Preview
@Composable
fun MenuItemPreview() {
    MenuItem(
        title = "Sports",
        leadingIcon = R.drawable.ic_filter_list,
        isSelected = true,
        onItemClick = {})
}
