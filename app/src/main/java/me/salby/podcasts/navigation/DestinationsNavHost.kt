package me.salby.podcasts.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.salby.podcasts.R
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.ui.collection.CollectionRoute
import me.salby.podcasts.ui.collection.CollectionViewModel
import me.salby.podcasts.ui.home.HomeRoute
import me.salby.podcasts.ui.home.HomeViewModel
import me.salby.podcasts.ui.theme.TopLevelEnterTransition
import me.salby.podcasts.ui.theme.TopLevelExitTransition

sealed class TopLevelDestination(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
) {
    data object Home : TopLevelDestination(
        "home",
        R.string.home,
        Icons.Outlined.Home,
        Icons.Filled.Home
    )

    data object Discover : TopLevelDestination(
        "discover",
        R.string.discover,
        Icons.Outlined.Podcasts
    )

    data object Collection : TopLevelDestination(
        "collection",
        R.string.collection,
        Icons.Outlined.FavoriteBorder,
        Icons.Outlined.Favorite
    )

    companion object {
        val all = listOf(Home, Discover, Collection)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DestinationsNavHost(
    onNavigateToFeed: (Feed) -> Unit,
    onNavigateToSearchResultFeed: (Feed) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sharedTransitionScope: SharedTransitionScope? = null
) {
    NavHost(
        navController,
        startDestination = TopLevelDestination.Home.route,
        modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceContainer),
        enterTransition = { TopLevelEnterTransition() },
        exitTransition = { TopLevelExitTransition() },
        popEnterTransition = { TopLevelEnterTransition() },
        popExitTransition = { TopLevelExitTransition() }
    ) {
        composable(TopLevelDestination.Home.route) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeRoute(
                homeViewModel,
                onNavigateToFeed,
                onNavigateToSearchResultFeed,
                onNavigateToSubscriptions,
                sharedTransitionScope,
                animatedVisibilityScope = this@composable
            )
        }

        composable(TopLevelDestination.Discover.route) {
            Text("Discover")
        }

        composable(TopLevelDestination.Collection.route) {
            val collectionViewModel = hiltViewModel<CollectionViewModel>()
            CollectionRoute(
                collectionViewModel
            )
        }
    }
}