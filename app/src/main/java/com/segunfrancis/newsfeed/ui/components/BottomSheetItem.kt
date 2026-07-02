package com.segunfrancis.newsfeed.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segunfrancis.newsfeed.R
import kotlinx.coroutines.launch

@Composable
fun OptionItem(@StringRes title: Int, @DrawableRes iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = stringResource(title))
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonOptionBottomSheet(
    isBookmarked: Boolean,
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    onShareClick:() -> Unit,
    onBookmarkClick: () -> Unit
) {
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = { /*viewModel.setSelectedArticle(null)*/ onDismissRequest() },
        sheetMaxWidth = BottomSheetDefaults.SheetMaxWidth,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Transparent) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true)
    ) {
        val scope = rememberCoroutineScope()
        Column {
            OptionItem(
                title = if (isBookmarked) R.string.remove_from_save else R.string.save_for_later,
                iconRes = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline,
                onClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            onBookmarkClick()
                        }
                    }
                }
            )
            Spacer(Modifier.height(4.dp))
            OptionItem(
                title = R.string.share,
                iconRes = R.drawable.ic_share,
                onClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            onShareClick()
                        }
                    }
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
