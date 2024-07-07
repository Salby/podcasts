package me.salby.podcasts.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.domain.ObserveSubscriptionsUseCase
import javax.inject.Inject

sealed interface CollectionUiState {
    val isLoading: Boolean

    data class NoFeeds(
        override val isLoading: Boolean
    ) : CollectionUiState

    data class HasFeeds(
        val feeds: List<Feed>,
        override val isLoading: Boolean
    ) : CollectionUiState
}

private data class CollectionViewModelState(
    val feeds: List<Feed>? = null,
    val isLoading: Boolean = false
) {
    /**
     * Converts this [CollectionViewModelState] into a more strongly typed [CollectionUiState] for
     * driving the UI.
     */
    fun toUiState() =
        if (feeds != null) CollectionUiState.HasFeeds(feeds, isLoading)
        else CollectionUiState.NoFeeds(isLoading)
}

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val observeSubscriptionsUseCase: ObserveSubscriptionsUseCase
) : ViewModel() {
    private val viewModelState = MutableStateFlow(CollectionViewModelState(isLoading = true))

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        viewModelScope.launch { observeSubscriptions() }
    }

    private suspend fun observeSubscriptions() {
        observeSubscriptionsUseCase().collect { feeds ->
            viewModelState.update {
                it.copy(
                    feeds = feeds,
                    isLoading = false
                )
            }
        }
    }
}