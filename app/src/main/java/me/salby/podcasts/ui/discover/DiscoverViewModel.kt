package me.salby.podcasts.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.salby.podcasts.data.podcasts.model.Feed
import javax.inject.Inject

sealed interface DiscoverUiState {
    val isLoading: Boolean

    data class HasFeeds(
        val feeds: List<Feed>,
        override val isLoading: Boolean
    ) : DiscoverUiState

    data class NoFeeds(
        override val isLoading: Boolean
    ) : DiscoverUiState
}

private data class DiscoverViewModelState(
    val feeds: List<Feed>? = null,
    val isLoading: Boolean = false
) {
    /**
     * Converts this [DiscoverViewModelState] into a more strongly typed [DiscoverUiState] for
     * driving the UI.
     */
    fun toUiState() =
        if (feeds != null) DiscoverUiState.HasFeeds(feeds, isLoading)
        else DiscoverUiState.NoFeeds(isLoading)
}

@HiltViewModel
class DiscoverViewModel @Inject constructor() : ViewModel() {
    private val viewModelState = MutableStateFlow(DiscoverViewModelState(isLoading = true))

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )
}