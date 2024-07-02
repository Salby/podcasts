package me.salby.podcasts.data.podcasts.model

import androidx.room.Embedded
import androidx.room.Relation

data class ProgressWithEpisode(
    @Embedded
    val progress: Progress,

    @Relation(parentColumn = "episode_id", entityColumn = "id")
    val episode: Episode
)