package me.salby.podcasts.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import me.salby.podcasts.R
import me.salby.podcasts.ui.Navigation
import me.salby.podcasts.ui.feed.FeedRoute
import me.salby.podcasts.ui.feed.FeedViewModel
import me.salby.podcasts.ui.home.HomeRoute
import me.salby.podcasts.ui.home.HomeViewModel
import me.salby.podcasts.ui.subscriptions.SubscriptionsRoute
import me.salby.podcasts.ui.subscriptions.SubscriptionsViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PodcastsNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    SharedTransitionLayout(modifier) {
        NavHost(
            navController,
            startDestination = "/",
            modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            composable(
                "/",
                exitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                }
            ) {
                Navigation(
                    onNavigateToFeed = {
                        navController.navigate("feed/${it.id}")
                    },
                    onNavigateToSearchResultFeed = {
                        navController.navigate("search/${it.podcastIndexOrgId}")
                    },
                    onNavigateToSubscriptions = {
                        navController.navigate("subscriptions")
                    },
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }

            composable(
                "home",
                exitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                }
            ) {
                val homeViewModel = hiltViewModel<HomeViewModel>()
                HomeRoute(
                    homeViewModel,
                    onNavigateToFeed = { navController.navigate("feed/${it.id}") },
                    onNavigateToSearchResultFeed = { navController.navigate("search/${it.podcastIndexOrgId}") },
                    onNavigateToSubscriptions = { navController.navigate("subscriptions") },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }

            composable(
                "subscriptions",
                enterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                }
            ) {
                val subscriptionsViewModel = hiltViewModel<SubscriptionsViewModel>()
                SubscriptionsRoute(
                    subscriptionsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFeed = { navController.navigate("home/subscriptions/feed/${it.id}") },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }

            composable(
                "home/subscriptions/feed/{feedId}",
                arguments = listOf(
                    navArgument("feedId") { type = NavType.IntType }
                ),
                enterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                }
            ) {
                val feedViewModel = hiltViewModel<FeedViewModel>()
                FeedRoute(
                    viewModel = feedViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }

            composable(
                "search/{podcastIndexOrgId}",
                arguments = listOf(
                    navArgument("podcastIndexOrgId") { type = NavType.LongType }
                )
            ) {
                val feedViewModel = hiltViewModel<FeedViewModel>()
                FeedRoute(
                    viewModel = feedViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .wrapContentWidth()
                                .sharedBounds(
                                    rememberSharedContentState("feed-search"),
                                    this@composable,
                                    zIndexInOverlay = 1f
                                )
                                .skipToLookaheadSize()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.navigate_back)
                            )
                        }
                    }
                )
            }

            composable(
                "feed/{feedId}",
                arguments = listOf(
                    navArgument("feedId") { type = NavType.IntType }
                ),
                enterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(tween(500, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(tween(500, easing = FastOutSlowInEasing))
                }
            ) {
                val feedViewModel = hiltViewModel<FeedViewModel>()
                FeedRoute(
                    viewModel = feedViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable
                )
            }
        }
    }
}