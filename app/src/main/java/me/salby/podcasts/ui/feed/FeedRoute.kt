package me.salby.podcasts.ui.feed

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedRoute(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    navigationIcon: (@Composable () -> Unit)? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    FeedRoute(
        uiState, onNavigateBack,
        onSubscribe = { viewModel.subscribeToFeed() },
        onUnsubscribe = { viewModel.unsubscribeFromFeed() },
        modifier,
        navigationIcon,
        sharedTransitionScope, animatedVisibilityScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedRoute(
    uiState: FeedUiState,
    onNavigateBack: () -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    CompactFeedScreen(
        uiState, onNavigateBack, onSubscribe, onUnsubscribe, modifier, navigationIcon,
        sharedTransitionScope, animatedVisibilityScope
    )
}