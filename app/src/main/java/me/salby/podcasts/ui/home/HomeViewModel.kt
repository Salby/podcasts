package me.salby.podcasts.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import me.salby.podcasts.data.podcasts.PodcastsRepository
import me.salby.podcasts.data.podcasts.model.Feed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.salby.podcasts.data.player.PlayerService
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode
import javax.inject.Inject

data class HomeMessage(
    val id: Long,
    val message: Int
)

/**
 * UI state for the home route.
 */
sealed interface HomeUiState {
    val query: String
    val searchResult: List<Feed>
    val searchIsActive: Boolean
    val isLoading: Boolean
    val messages: List<HomeMessage>

    /**
     * There are no feeds to show.
     *
     * This could be because the user isn't subscribed to any feeds, or because they haven't loaded
     * yet.
     */
    data class NoFeeds(
        override val query: String,
        override val searchResult: List<Feed>,
        override val searchIsActive: Boolean,
        override val isLoading: Boolean,
        override val messages: List<HomeMessage>
    ) : HomeUiState

    /**
     * There are feeds to show.
     */
    data class HasFeeds(
        val feeds: List<Feed>,
        val latestEpisode: ProgressWithEpisode?,
        override val query: String,
        override val searchResult: List<Feed>,
        override val searchIsActive: Boolean,
        override val isLoading: Boolean,
        override val messages: List<HomeMessage>
    ) : HomeUiState
}

/**
 * An internal representation of the home route state.
 */
private data class HomeViewModelState(
    val feeds: List<Feed>? = null,
    val latestEpisode: ProgressWithEpisode? = null,
    val query: String = "",
    val searchResult: List<Feed> = emptyList(),
    val searchIsActive: Boolean = false,
    val isLoading: Boolean = false,
    val messages: List<HomeMessage> = emptyList()
) {
    /**
     * Converts this [HomeViewModelState] to a more strongly typed [HomeUiState] for driving the
     * UI.
     */
    fun toUiState(): HomeUiState =
        if (!feeds.isNullOrEmpty()) {
            HomeUiState.HasFeeds(
                feeds, latestEpisode, query, searchResult, searchIsActive, isLoading, messages
            )
        } else {
            HomeUiState.NoFeeds(
                query, searchResult, searchIsActive, isLoading, messages
            )
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val playerService: PlayerService
) : ViewModel() {
    private val viewModelState = MutableStateFlow(HomeViewModelState(isLoading = true))

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        collectSearchResults()
        observeSubscriptions()

        // Find and display the episode that the user listened to last.
        viewModelScope.launch {
            val allProgress = podcastsRepository
                .observeProgressForAllEpisodes()
                .first()
            Log.d("Progress", "${allProgress.size}")
            val latestEpisodeWithProgress = allProgress
                .firstOrNull { (progress, episode) ->
                    progress.progress > 0 && episode.duration.inWholeMilliseconds - progress.progress > 10000
                } ?: return@launch
            Log.d("Progress", "$latestEpisodeWithProgress")
            // We don't want to display the episode if it is the current episode in the player.
            val currentEpisode = playerService.mediaItem.first().let {
                it?.mediaId?.let {  mediaId ->
                    val (_, episodeId) = PlayerService.getFeedAndEpisodeIdsFromMediaId(mediaId)
                    podcastsRepository.getEpisodeById(episodeId)
                }
            }
            if (latestEpisodeWithProgress.episode == currentEpisode) {
                return@launch
            }
            viewModelState.update {
                it.copy(latestEpisode = latestEpisodeWithProgress)
            }
        }
    }

    fun setQuery(newQuery: String) {
        viewModelScope.launch {
            viewModelState.update { it.copy(query = newQuery) }
        }
    }

    fun setSearchIsActive(isActive: Boolean) {
        viewModelScope.launch {
            viewModelState.update { it.copy(searchIsActive = isActive) }
        }
    }

    private fun collectSearchResults() {
        viewModelScope.launch {
            viewModelState
                .map { it.query.trim() }
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isEmpty()) {
                        viewModelState.update { it.copy(searchResult = emptyList()) }
                        return@collect
                    }

                    val result = podcastsRepository.searchPodcastFeeds(query)
                    viewModelState.update { it.copy(searchResult = result) }
                }
        }
    }

    private fun observeSubscriptions() {
        viewModelScope.launch {
            podcastsRepository
                .observeSubscriptions()
                .collect { feeds ->
                    viewModelState.update {
                        it.copy(
                            feeds = feeds,
                            isLoading = false
                        )
                    }
                }
        }
    }
}