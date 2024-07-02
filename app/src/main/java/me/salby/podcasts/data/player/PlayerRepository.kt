package me.salby.podcasts.data.player

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.salby.podcasts.data.podcasts.PodcastsRepository
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.di.DefaultDispatcher
import javax.inject.Inject

private data class InternalPlayerState(
    val isPlaying: Boolean = false,
    val currentEpisode: Episode? = null,
    val currentPosition: Long = 0,
    val currentFeed: Feed? = null,
    val isLoading: Boolean = false
) {
    fun toPlayerState(): PlayerState =
        if (currentEpisode != null && currentFeed != null) {
            PlayerState.Active(
                isPlaying, currentEpisode, currentPosition, currentFeed, isLoading
            )
        } else {
            PlayerState.None(isLoading)
        }
}

class PlayerRepository @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val externalScope: CoroutineScope,
    private val playerService: PlayerService,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    private val state = MutableStateFlow(InternalPlayerState())
    val playerState: Flow<PlayerState> = state.map { it.toPlayerState() }

    init {
        observePlayer()
        updateProgress()
    }

    fun play() = externalScope.launch {
        playerService.play()
    }

    fun pause() = externalScope.launch {
        playerService.pause()
    }

    fun seekNext() = externalScope.launch {
        playerService.seekNext()
    }

    fun seekPrevious() = externalScope.launch {
        playerService.seekPrevious()
    }

    fun setPosition(positionMs: Long) = externalScope.launch {
        playerService.setPosition(positionMs)
    }

    fun playEpisode(episodeId: Int) = externalScope.launch(defaultDispatcher) {
        setEpisode(episodeId)
        with(playerService) {
            prepare()
            play()
        }
    }

    fun playEpisode(episodeId: Int, fromPosition: Long) = externalScope.launch {
        val episode = podcastsRepository.getEpisodeById(episodeId) ?: return@launch
        playerService.setMediaItem(PlayerService.buildMediaItemFromEpisode(episode), fromPosition)
        with(playerService) {
            prepare()
            play()
        }
    }

    private suspend fun setEpisode(episodeId: Int, resetPosition: Boolean = true) {
        val episode = podcastsRepository.getEpisodeById(episodeId) ?: return
        with(playerService) {
            setMediaItem(PlayerService.buildMediaItemFromEpisode(episode), resetPosition)
        }
    }

    fun addEpisodeToQueue(episodeId: Int) = externalScope.launch(defaultDispatcher) {
        val episode = podcastsRepository.getEpisodeById(episodeId) ?: return@launch
        playerService.addMediaItem(PlayerService.buildMediaItemFromEpisode(episode))
    }

    private suspend fun getEpisodeAndFeedFromMediaId(mediaId: String): Pair<Episode, Feed> {
        val (feedId, episodeId) = mediaId.split('-').let {
            it.first().toInt() to it.last().toInt()
        }
        val feed = podcastsRepository.getLocalFeedById(feedId)
            ?: throw Exception("Feed with id $feedId doesn't exist.")
        val episode = podcastsRepository.getEpisodeById(episodeId)
            ?: throw Exception("Episode with id $episodeId doesn't exist.")
        return episode to feed
    }

    private fun updateCurrentEpisodeForUi(mediaId: String) = externalScope.launch {
        val (episode, feed) = getEpisodeAndFeedFromMediaId(mediaId)
        state.update {
            it.copy(
                currentEpisode = episode,
                currentFeed = feed
            )
        }
    }

    private fun updateProgress() = externalScope.launch {
        playerService.mediaItem
            .combine(playerService.position) { mediaItem, position -> mediaItem to position }
            .collect { (mediaItem, position) ->
                if (mediaItem == null) {
                    return@collect
                }
                val episodeId = mediaItem.mediaId.split("-").last().toInt()
                podcastsRepository.updateProgress(position, episodeId)
            }
    }

    private fun observePlayer() {
        externalScope.launch {
            playerService.isPlaying.collect { isPlaying ->
                state.update {
                    it.copy(isPlaying = isPlaying)
                }
            }
        }
        externalScope.launch {
            playerService.isLoading.collect { isLoading ->
                state.update {
                    it.copy(isLoading = isLoading)
                }
            }
        }
        externalScope.launch {
            playerService.position.collect { position ->
                state.update {
                    it.copy(currentPosition = position)
                }
            }
        }
        externalScope.launch {
            playerService.mediaItem.collect { mediaItem ->
                mediaItem?.mediaId?.let { mediaId ->
                    updateCurrentEpisodeForUi(mediaId)
                }
            }
        }
    }
}