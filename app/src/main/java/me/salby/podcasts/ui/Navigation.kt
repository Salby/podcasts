package me.salby.podcasts.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.navigation.DestinationsNavHost
import me.salby.podcasts.navigation.TopLevelDestination
import me.salby.podcasts.ui.theme.EmphasizedAccelerate
import me.salby.podcasts.ui.theme.EmphasizedDecelerate
import me.salby.podcasts.ui.theme.PodcastsTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Navigation(
    onNavigateToFeed: (Feed) -> Unit,
    onNavigateToSearchResultFeed: (Feed) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBarScaffold(
        destinations = TopLevelDestination.all,
        selectedRoute = currentDestination?.route,
        onNavigateToDestination = {
            navController.navigate(it.route) {
                // Pop to the start destination of the graph to avoid building up a large stack of
                // destinations on the back stack as the user selects items.
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }

                // Avoid multiple copies of the same destination when selecting the currently
                // selected item.
                launchSingleTop = true

                // Restore state when selecting a previously selected item.
                restoreState = true
            }
        },
        modifier = modifier,
        navigationBarIsHidden = TopLevelDestination.all.firstOrNull { it.route == currentDestination?.route } != null
    ) { innerPadding ->
        DestinationsNavHost(
            onNavigateToFeed,
            onNavigateToSearchResultFeed,
            onNavigateToSubscriptions,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            navController = navController,
            sharedTransitionScope
        )
    }
}

@Composable
private fun NavigationBarScaffold(
    destinations: List<TopLevelDestination>,
    selectedRoute: String?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    navigationBarIsHidden: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedContent(
                targetState = navigationBarIsHidden,
                transitionSpec = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(
                            400, 200,
                            EmphasizedDecelerate
                        )
                    ) togetherWith slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(
                            200,
                            easing = EmphasizedAccelerate
                        )
                    )
                },
                label = "Navigation bar visibility"
            ) { hideNavigationBar ->
                if (!hideNavigationBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        destinations.forEach {
                            val isSelected by remember(selectedRoute) {
                                derivedStateOf {
                                    it.route == selectedRoute
                                }
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { onNavigateToDestination(it) },
                                icon = {
                                    Icon(
                                        if (isSelected && it.selectedIcon != null) it.selectedIcon
                                        else it.icon,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(stringResource(it.resourceId))
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun NavigationBarScaffoldPreview() {
    PodcastsTheme {
        NavigationBarScaffold(
            destinations = listOf(
                TopLevelDestination.Home,
                TopLevelDestination.Discover,
                TopLevelDestination.Collection
            ),
            selectedRoute = TopLevelDestination.Discover.route,
            onNavigateToDestination = {}
        ) {
            Text("Navigation bar scaffold")
        }
    }
}