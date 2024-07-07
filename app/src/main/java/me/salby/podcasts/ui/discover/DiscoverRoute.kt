package me.salby.podcasts.ui.discover

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DiscoverRoute(
        uiState
    )
}

@Composable
fun DiscoverRoute(
    uiState: DiscoverUiState
) {
    CompactDiscoverScreen(
        uiState
    )
}