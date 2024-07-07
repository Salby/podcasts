package me.salby.podcasts.ui.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CollectionRoute(
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CollectionRoute(
        uiState
    )
}

@Composable
fun CollectionRoute(
    uiState: CollectionUiState
) {
    CompactCollectionScreen(
        uiState
    )
}