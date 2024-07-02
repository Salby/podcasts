package me.salby.podcasts.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.salby.podcasts.data.podcasts.PodcastsRepository
import me.salby.podcasts.data.podcasts.model.Feed
import javax.inject.Inject

/**
 * The UI state for the subscriptions route.
 */
sealed interface SubscriptionsUiState {
    val isLoading: Boolean

    data class HasFeeds(
        val feeds: List<Feed>,
        override val isLoading: Boolean
    ) : SubscriptionsUiState

    data class EditFeeds(
        val feeds: List<Feed>,
        val selectedFeeds: List<Int>,
        override val isLoading: Boolean
    ) : SubscriptionsUiState

    data class NoFeeds(
        override val isLoading: Boolean
    ) : SubscriptionsUiState
}

/**
 * An internal representation of the state of the subscriptions route.
 */
private data class SubscriptionsViewModelState(
    val feeds: List<Feed>? = null,
    val selectedFeeds: List<Int> = emptyList(),
    val isLoading: Boolean = false
) {
    /**
     * Converts this [SubscriptionsViewModelState] into a more strongly typed
     * [SubscriptionsUiState] for driving the UI.
     */
    fun toUiState() =
        when {
            feeds != null && selectedFeeds.isNotEmpty() -> SubscriptionsUiState.EditFeeds(
                feeds, selectedFeeds, isLoading
            )

            feeds != null -> SubscriptionsUiState.HasFeeds(
                feeds, isLoading
            )

            else -> SubscriptionsUiState.NoFeeds(
                isLoading
            )
        }
}

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository
) : ViewModel() {
    private val viewModelState = MutableStateFlow(SubscriptionsViewModelState(isLoading = true))

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        observeSubscriptions()
    }

    fun selectFeed(feedId: Int) = viewModelScope.launch {
        viewModelState.update {
            val selectedFeeds = it.selectedFeeds.toMutableList()
            if (!selectedFeeds.contains(feedId)) {
                selectedFeeds.add(feedId)
            }
            it.copy(selectedFeeds = selectedFeeds)
        }
    }

    fun deselectFeed(feedId: Int) = viewModelScope.launch {
        viewModelState.update {
            val selectedFeeds = it.selectedFeeds.filterNot { id -> id == feedId }
            it.copy(selectedFeeds = selectedFeeds)
        }
    }

    fun removeFeedFromSubscriptions(feedId: Int) = viewModelScope.launch {
        viewModelState.value.feeds?.first { it.id == feedId }?.let { feed ->
            podcastsRepository.unsubscribeFromFeed(feed)
            deselectFeed(feed.id)
        }
    }

    private fun observeSubscriptions() = viewModelScope.launch {
        viewModelState.update {
            it.copy(isLoading = true)
        }
        podcastsRepository.observeSubscriptions().collect { feeds ->
            viewModelState.update {
                it.copy(
                    feeds = feeds,
                    isLoading = false
                )
            }
        }
    }
}