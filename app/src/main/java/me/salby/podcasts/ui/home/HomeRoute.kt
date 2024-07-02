package me.salby.podcasts.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import me.salby.podcasts.data.podcasts.model.Feed

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToFeed: (Feed) -> Unit,
    onNavigateToSearchResultFeed: (Feed) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeRoute(
        uiState,
        onQueryChange = { viewModel.setQuery(it) },
        onSetSearchIsActive = { viewModel.setSearchIsActive(it) },
        onNavigateToFeed,
        onNavigateToSearchResultFeed,
        onNavigateToSubscriptions,
        sharedTransitionScope,
        animatedVisibilityScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeRoute(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSetSearchIsActive: (Boolean) -> Unit,
    onNavigateToFeed: (Feed) -> Unit,
    onNavigateToSearchResultFeed: (Feed) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    CompactHomeScreen(
        uiState, onQueryChange,
        onSearchIsExpandedChange = onSetSearchIsActive,
        onNavigateToFeed, onNavigateToSearchResultFeed, onNavigateToSubscriptions,
        sharedTransitionScope, animatedVisibilityScope
    )
}