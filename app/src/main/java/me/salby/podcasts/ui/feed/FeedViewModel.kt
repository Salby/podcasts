package me.salby.podcasts.ui.feed

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.salby.podcasts.data.player.PlayerService
import me.salby.podcasts.data.podcasts.PodcastsRepository
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode
import javax.inject.Inject

data class FeedMessage(
    val id: Long,
    val message: Int
)

sealed interface FeedUiState {
    val id: Int
    val isLoading: Boolean
    val messages: List<FeedMessage>

    data class HasFeed(
        val feed: Feed,
        val episodes: List<Episode>,
        val lastListened: ProgressWithEpisode?,
        override val id: Int,
        override val isLoading: Boolean,
        override val messages: List<FeedMessage>
    ) : FeedUiState

    data class NoFeed(
        override val id: Int,
        override val isLoading: Boolean,
        override val messages: List<FeedMessage>
    ) : FeedUiState
}

private data class FeedViewModelState(
    val feed: Feed? = null,
    val episodes: List<Episode> = emptyList(),
    val lastListened: ProgressWithEpisode? = null,
    val id: Int,
    val isLoading: Boolean = false,
    val messages: List<FeedMessage> = emptyList()
) {
    fun toUiState(): FeedUiState =
        if (feed != null) {
            FeedUiState.HasFeed(
                feed, episodes, lastListened, id, isLoading, messages
            )
        } else {
            FeedUiState.NoFeed(
                id, isLoading, messages
            )
        }
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PodcastsRepository,
    private val playerService: PlayerService
) : ViewModel() {
    private val viewModelState = MutableStateFlow(
        FeedViewModelState(
            id = savedStateHandle.get<Int>("feedId") ?: -1,
            isLoading = true
        )
    )

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        observeFeed()
        continueListening()
    }

    fun subscribeToFeed() {
        viewModelScope.launch {
            viewModelState.value.feed?.let {
                repository.subscribeToFeed(it)
            }
        }
    }

    fun unsubscribeFromFeed() {
        viewModelScope.launch {
            viewModelState.value.feed?.let {
                repository.unsubscribeFromFeed(it)
            }
        }
    }

    private fun observeFeed() {
        viewModelScope.launch {
            viewModelState.update { it.copy(isLoading = true) }
            if (viewModelState.value.id <= 0) {
                // Attempt to fetch feed data from podcast index.
                val id = savedStateHandle.get<Long>("podcastIndexOrgId")
                if (id != null) {
                    try {
                        setFeedIdByPodcastIndexOrgId(id)
                    } catch (e: Exception) {
                        Log.d("FeedViewModel.observeFeed", "Feed doesn't exist anywhere")
                    }
                }
            }
            repository
                .observeFeedByIdWithEpisodes(viewModelState.value.id)
                .onEach { feedWithEpisodes ->
                    if (feedWithEpisodes != null && feedWithEpisodes.episodes.isEmpty()) {
                        fetchEpisodes(
                            feedWithEpisodes.feed.podcastIndexOrgId,
                            feedWithEpisodes.feed.id
                        )
                    }
                }
                .collect { feedWithEpisodes ->
                    if (feedWithEpisodes != null) {
                        viewModelState.update {
                            it.copy(
                                feed = feedWithEpisodes.feed,
                                episodes = feedWithEpisodes.episodes
                            )
                        }
                    }
                    viewModelState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun continueListening() = viewModelScope.launch {
        val progress =  repository
            .observeProgressForAllEpisodes()
            .map {
                it.filter { (progress, episode) ->
                    episode.feedId == viewModelState.value.feed?.id &&
                            progress.progress > 0 &&
                            episode.duration.inWholeMilliseconds - progress.progress > 10000
                }
            }
            .first()
        if (progress.isEmpty()) {
            return@launch
        }
        val lastListened = progress.first()
        viewModelState.update {
            it.copy(lastListened = lastListened)
        }
        // Observe the player to remove the 'last listened' episode if the user resumes playing it.
        playerService.mediaItem.collect { mediaItem ->
            if (mediaItem == null) {
                return@collect
            }
            val (_, episodeId) = PlayerService.getFeedAndEpisodeIdsFromMediaId(mediaItem.mediaId)
            if (episodeId == lastListened.episode.id) {
                viewModelState.update {
                    it.copy(lastListened = null)
                }
            }
        }
    }

    private suspend fun setFeedIdByPodcastIndexOrgId(id: Long) {
        var feed = repository
            .getFeedById(id, refresh = true)
            ?: throw Exception("Feed doesn't exist.")
        if (feed.id < 1) {
            val insertId = repository.insertFeeds(feed).first()
            feed = feed.copy(id = insertId.toInt())
        }
        viewModelState.update { it.copy(id = feed.id) }
    }

    private suspend fun fetchEpisodes(feedId: Long, localId: Int) {
        val episodes = repository
            .getEpisodesByFeed(feedId, refresh = true)
            .map { it.copy(feedId = localId) }
        repository.insertEpisodes(*episodes.toTypedArray())
    }
}