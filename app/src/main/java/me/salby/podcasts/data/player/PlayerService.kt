package me.salby.podcasts.data.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.salby.podcasts.PlaybackService
import me.salby.podcasts.data.podcasts.PodcastsRepository
import me.salby.podcasts.data.podcasts.model.Episode
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.coroutines.resume

class PlayerService @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val podcastsRepository: PodcastsRepository,
    private val externalScope: CoroutineScope,
) {
    private lateinit var mediaController: MediaController

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var timer: Timer? = null
    private var nextEpisodeJob: Job? = null
    private val playerIsPlaying = MutableStateFlow(false)
    private val playerIsLoading = MutableStateFlow(false)
    private val playerPosition = MutableStateFlow<Long>(0)
    private val playerMediaItem = MutableStateFlow<MediaItem?>(null)

    val isPlaying = playerIsPlaying.asSharedFlow()
    val isLoading = playerIsLoading.asSharedFlow()
    val position = playerPosition.asSharedFlow()
    val mediaItem = playerMediaItem.asSharedFlow()

    init {
        playerIsLoading.update { true }
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            addListeners()
            playerPosition.update { mediaController.currentPosition }
            playerMediaItem.update { mediaController.currentMediaItem }
            playerIsLoading.update { false }
        }, MoreExecutors.directExecutor())
    }

    suspend fun play() = withContext(mainDispatcher) {
        mediaController.play()
    }

    suspend fun pause() = withContext(mainDispatcher) {
        mediaController.pause()
    }

    suspend fun prepare() = withContext(mainDispatcher) {
        mediaController.prepare()
    }

    suspend fun seekNext() = withContext(mainDispatcher) {
        mediaController.seekToNext()
    }

    suspend fun seekPrevious() = withContext(mainDispatcher) {
        mediaController.seekToPrevious()
    }

    suspend fun setPosition(positionMs: Long) = withContext(mainDispatcher) {
        mediaController.seekTo(positionMs)
    }

    suspend fun addMediaItem(mediaItem: MediaItem) = withContext(mainDispatcher) {
        mediaController.addMediaItem(mediaItem)
    }

    suspend fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean = true) =
        withContext(mainDispatcher) {
            mediaController.setMediaItem(mediaItem, resetPosition)
        }

    suspend fun setMediaItem(
        mediaItem: MediaItem,
        positionMs: Long,
        resetPosition: Boolean = true
    ) = externalScope.launch(mainDispatcher) {
        mediaController.setMediaItem(mediaItem, resetPosition)
        mediaController.prepare()
        suspendCancellableCoroutine { continuation ->
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        mediaController.seekTo(positionMs)
                        mediaController.removeListener(this)
                        continuation.resume(Unit)
                    }
                }
            }
            mediaController.addListener(listener)
        }
    }

    suspend fun addMediaItem(index: Int, mediaItem: MediaItem) = withContext(mainDispatcher) {
        mediaController.addMediaItem(index, mediaItem)
    }

    private fun addListeners() {
        mediaController.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playerIsPlaying.update { isPlaying }
                    if (isPlaying) {
                        recordPosition()
                    } else {
                        stopRecordingPosition()
                    }
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    playerIsLoading.update { isLoading }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    playerMediaItem.update { mediaItem }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        if (mediaController.hasNextMediaItem() || nextEpisodeJob != null) {
                            return
                        }
                        nextEpisodeJob = externalScope.launch { playNextEpisode() }
                    }
                }
            }
        )
    }

    private suspend fun playNextEpisode() {
        // Play the next episode of the podcast if available.
        val (feedId, episodeId) = withContext(mainDispatcher) {
            mediaController.currentMediaItem?.mediaId?.let {
                getFeedAndEpisodeIdsFromMediaId(it)
            }
        } ?: return
        val finishedEpisode = podcastsRepository.getEpisodeById(episodeId)
        val episodes = podcastsRepository
            .observeFeedByIdWithEpisodes(feedId)
            .firstOrNull()
            ?.episodes
        if (finishedEpisode == null || episodes == null) {
            return
        }
        val nextEpisode = episodes.firstOrNull { episode ->
            episode.episode == finishedEpisode.episode + 1
        } ?: return
        withContext(mainDispatcher) {
            mediaController.run {
                if (hasNextMediaItem()) {
                    return@withContext
                }
                addMediaItem(buildMediaItemFromEpisode(nextEpisode))
                seekToNextMediaItem()
                prepare()
                play()
            }
        }
    }

    private fun recordPosition() {
        if (timer == null) {
            timer = Timer()
        }
        timer?.scheduleAtFixedRate(
            delay = 0,
            period = 500,
            action = {
                externalScope.launch(mainDispatcher) {
                    playerPosition.update { mediaController.currentPosition }
                }
            }
        )
    }

    private fun stopRecordingPosition() {
        timer?.cancel()
        timer = null
    }

    companion object {
        fun buildMediaItemFromEpisode(episode: Episode): MediaItem =
            MediaItem.Builder()
                .setMediaId("${episode.feedId}-${episode.id}")
                .setUri(episode.source)
                .build()

        fun getFeedAndEpisodeIdsFromMediaId(mediaId: String): Pair<Int, Int> =
            mediaId.split("-").let {
                it.first().toInt() to it.last().toInt()
            }
    }
}