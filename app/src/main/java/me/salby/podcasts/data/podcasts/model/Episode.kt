package me.salby.podcasts.data.podcasts.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Entity(
    tableName = "episodes",
    indices = [Index("podcast_index_org_id", unique = true)]
)
data class Episode(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true)
    val id: Int = 0,

    val title: String,
    val description: String,
    val image: String,
    @ColumnInfo("is_explicit") val isExplicit: Boolean,
    val duration: Duration,
    val published: LocalDateTime,
    val episode: Int = 0,
    val season: Int = 0,
    @ColumnInfo("feed_id") val feedId: Int = 0,
    val source: String,
    @ColumnInfo("source_type") val sourceType: String,
    @ColumnInfo("source_length") val sourceLength: Long,

    @ColumnInfo("podcast_index_org_id") val podcastIndexOrgId: Long = 0,
    @ColumnInfo("podcast_index_org_guid") val podcastIndexOrgGuid: String = ""

    /*val id: Int,
    val title: String,
    val link: String,
    val description: String,
    val guid: String,

    /**
     * The time the [episode][Episode] was published, in epoch seconds.
     */
    val datePublished: Long,

    val enclosureUrl: String,
    val enclosureType: String,
    val enclosureLength: Long,

    /**
     * Duration of the episode in seconds.
     *
     * Null if the [episode][Episode] is live.
     */
    val duration: Long?,

    val explicit: Int,
    val episode: Int?,
    val season: Int?,
    val image: String,
    val feedId: Long,
    val podcastGuid: String*/
)

class EpisodeDeserializer : JsonDeserializer<Episode> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Episode {
        val episode = json?.asJsonObject?.get("episode")
        val season = json?.asJsonObject?.get("season")
        return Episode(
            title = json?.asJsonObject?.get("title")?.asString ?: "",
            description = json?.asJsonObject?.get("description")?.asString ?: "",
            image = json?.asJsonObject?.get("image")?.asString ?: "",
            isExplicit = (json?.asJsonObject?.get("explicit")?.asInt ?: 0) == 1,
            duration = (json?.asJsonObject?.get("duration")?.asInt ?: 0).seconds,
            published = LocalDateTime.ofEpochSecond(
                json?.asJsonObject?.get("publishedDate")?.asLong ?: 0, 0, ZoneOffset.UTC
            ),
            episode = if (episode?.isJsonNull == true) {
                0
            } else {
                episode?.asInt ?: 0
            },
            season = if (season?.isJsonNull == true) {
                0
            } else {
                season?.asInt ?: 0
            },
            source = json?.asJsonObject?.get("enclosureUrl")?.asString ?: "",
            sourceType = json?.asJsonObject?.get("enclosureType")?.asString ?: "",
            sourceLength = json?.asJsonObject?.get("enclosureLength")?.asLong ?: 0,
            podcastIndexOrgId = json?.asJsonObject?.get("id")?.asLong ?: 0,
            podcastIndexOrgGuid = json?.asJsonObject?.get("guid")?.asString ?: ""
        )
    }
}

data class EpisodesResult(
    val status: String,
    @SerializedName("items") val episodes: List<Episode>,
    val count: Int,
    val query: String,
    val description: String
)