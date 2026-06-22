package com.segunfrancis.newsfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.segunfrancis.newsfeed.ui.favourite.FavouriteScreen
import com.segunfrancis.newsfeed.ui.home.HomeScreen
import com.segunfrancis.newsfeed.ui.components.NewsFeedToolbar
import com.segunfrancis.newsfeed.ui.settings.SettingsScreen
import com.segunfrancis.newsfeed.ui.theme.NewsFeedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedTheme {
                val navController = rememberNavController()
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                val snackbarHostState = remember { SnackbarHostState() }
                val scrollBehavior =
                    TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
                val lazyListState = rememberLazyListState()
                val visibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
                var isMenuExpanded by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                NavigationSuiteScaffold(
                    layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                        currentWindowAdaptiveInfo()
                    ),
                    navigationSuiteItems = {
                        TOP_LEVEL_NAV_ITEMS.forEach { (destinations, item) ->
                            val selected = destinations.route == currentDestination
                            item(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destinations.route) {
                                        restoreState = true
                                        launchSingleTop = true
                                        popUpTo(Home.route) {
                                            saveState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(if (selected) item.selectedIcon else item.unselectedIcon),
                                        contentDescription = stringResource(item.titleTextId)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(item.iconTextId))
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                NewsFeedToolbar(
                                    title = if (currentDestination == Favourite.route) Favourite.title else if (currentDestination == Settings.route) Settings.title else Home.title,
                                    scrollBehavior = scrollBehavior,
                                    isMenuExpanded = isMenuExpanded,
                                    onMenuAction = { navController.navigate(Settings.route) },
                                    onMenuItemClick = {  }
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        floatingActionButtonPosition = FabPosition.End,
                        floatingActionButton = {
                            AnimatedContent(
                                targetState = !lazyListState.isScrollInProgress && visibleItemIndex > 3,
                                label = "Fab animation container"
                            ) {
                                if (it) {
                                    FloatingActionButton(
                                        onClick = {
                                            scope.launch { lazyListState.animateScrollToItem(0) }
                                        },
                                        content = {
                                            Image(
                                                imageVector = Icons.Filled.KeyboardArrowUp,
                                                contentDescription = "Fab"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(route = Home.route) {
                                HomeScreen(
                                    snackbarHostState = snackbarHostState
                                )
                            }
                            composable(route = Favourite.route) {
                                FavouriteScreen()
                            }
                            composable(route = Settings.route) {
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NewsFeedTheme {

    }
}
