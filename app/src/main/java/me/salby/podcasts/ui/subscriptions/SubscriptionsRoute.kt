package me.salby.podcasts.ui.subscriptions

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import me.salby.podcasts.LocalPlayer
import me.salby.podcasts.data.podcasts.model.Feed

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SubscriptionsRoute(
    viewModel: SubscriptionsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToFeed: (Feed) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    SubscriptionsRoute(
        uiState, onNavigateBack, onNavigateToFeed,
        onSelectFeed = { viewModel.selectFeed(it) },
        onDeselectFeed = { viewModel.deselectFeed(it) },
        onUnsubscribe = { viewModel.removeFeedFromSubscriptions(it) },
        sharedTransitionScope, animatedVisibilityScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SubscriptionsRoute(
    uiState: SubscriptionsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToFeed: (Feed) -> Unit,
    onSelectFeed: (feedId: Int) -> Unit,
    onDeselectFeed: (feedId: Int) -> Unit,
    onUnsubscribe: (feedId: Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val player = LocalPlayer.current
    LaunchedEffect(uiState is SubscriptionsUiState.EditFeeds) {
        if (uiState is SubscriptionsUiState.EditFeeds) {
            player.hidePlayer()
        } else {
            player.showPlayer()
        }
    }

    CompactSubscriptionsScreen(
        uiState, onNavigateBack, onNavigateToFeed, onSelectFeed, onDeselectFeed, onUnsubscribe,
        sharedTransitionScope, animatedVisibilityScope
    )
}