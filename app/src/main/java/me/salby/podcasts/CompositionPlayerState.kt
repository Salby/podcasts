package me.salby.podcasts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.map
import me.salby.podcasts.data.player.PlayerRepository
import me.salby.podcasts.data.player.PlayerState

data class CompositionPlayerState(
    val state: PlayerState,
    val play: () -> Unit = {},
    val playEpisodeFromPosition: (episodeId: Int, positionMs: Long) -> Unit = { _, _ -> },
    val pause: () -> Unit = {},
    val seekNext: () -> Unit = {},
    val seekPrevious: () -> Unit = {},
    val setPosition: (Long) -> Unit = {},
    val playEpisode: (Int) -> Unit = {},
    val addEpisodeToQueue: (Int) -> Unit = {},
    var isHidden: Boolean = false
) {
    fun hidePlayer() {
        isHidden = false
    }

    fun showPlayer() {
        isHidden = true
    }
}

val LocalPlayer = compositionLocalOf { CompositionPlayerState(PlayerState.None(isLoading = false)) }

@Composable
fun ProvidePlayerState(
    repository: PlayerRepository,
    onPlay: () -> Unit = {},
    onPlayEpisodeFromPosition: (episodeId: Int, positionMs: Long) -> Unit,
    onPause: () -> Unit = {},
    onSeekNext: () -> Unit = {},
    onSeekPrevious: () -> Unit = {},
    onSetPosition: (Long) -> Unit = {},
    onPlayEpisode: (Int) -> Unit = {},
    onAddEpisodeToQueue: (Int) -> Unit = {},
    content: @Composable () -> Unit
) {
    val playerState = repository.playerState
        .map {
            CompositionPlayerState(
                it,
                onPlay,
                onPlayEpisodeFromPosition,
                onPause,
                onSeekNext,
                onSeekPrevious,
                onSetPosition,
                onPlayEpisode,
                onAddEpisodeToQueue
            )
        }
        .collectAsState(initial = CompositionPlayerState(PlayerState.None(isLoading = false)))
        .value
    ProvidePlayerState(playerState, content)
}

@Composable
fun ProvidePlayerState(
    compositionPlayerState: CompositionPlayerState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPlayer provides compositionPlayerState, content)
}