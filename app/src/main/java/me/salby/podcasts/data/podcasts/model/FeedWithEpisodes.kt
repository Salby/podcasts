package me.salby.podcasts.data.podcasts.model

import androidx.room.Embedded
import androidx.room.Relation

data class FeedWithEpisodes(
    @Embedded val feed: Feed,
    @Relation(parentColumn = "id", entityColumn = "feed_id") val episodes: List<Episode>
)