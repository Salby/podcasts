package me.salby.podcasts.data.player

import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

sealed interface PlayerState {
    val isLoading: Boolean

    data class None(
        override val isLoading: Boolean
    ) : PlayerState

    data class Active(
        val isPlaying: Boolean,
        val currentEpisode: Episode,
        val currentPosition: Long,
        val currentFeed: Feed,
        override val isLoading: Boolean
    ) : PlayerState {
        val image: String = if (
            currentEpisode.image.isNotBlank() &&
            currentEpisode.image.isNotEmpty()
        ) {
            currentEpisode.image
        } else {
            currentFeed.image
        }

        val timeLeft = (currentEpisode.duration - currentPosition.milliseconds)
    }
}