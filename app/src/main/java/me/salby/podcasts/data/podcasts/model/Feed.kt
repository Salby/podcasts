package me.salby.podcasts.data.podcasts.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDateTime

@Entity(tableName = "feeds", indices = [Index("podcast_index_org_id", unique = true)])
data class Feed(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val image: String,
    @ColumnInfo("is_explicit") val isExplicit: Boolean,
    val author: String,
    val language: String,
    @ColumnInfo("episode_count") val episodeCount: Int,
    val subscribed: LocalDateTime?,
    val url: String,
    val link: String,
    @ColumnInfo("podcast_index_org_id") val podcastIndexOrgId: Long = 0,
    @ColumnInfo("podcast_index_org_guid") val podcastIndexOrgGuid: String = ""

    /*val id: Long,
    val podcastGuid: String,
    val title: String,
    val url: String,
    val originalUrl: String,
    val link: String,
    val description: String,
    val author: String,
    val ownerName: String,
    val image: String,
    val artwork: String,
    val lastUpdateTime: Long,
    val contentType: String,
    val language: String,
    val explicit: Boolean,
    val dead: Int,
    val episodeCount: Int,
    val newestItemPubDate: Long*/
)

class FeedDeserializer : JsonDeserializer<Feed> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ) = Feed(
        title = json?.asJsonObject?.get("title")?.asString ?: "",
        description = json?.asJsonObject?.get("description")?.asString ?: "",
        image = json?.asJsonObject?.get("image")?.asString ?: "",
        isExplicit = (json?.asJsonObject?.get("explicit")?.asBoolean ?: false),
        author = json?.asJsonObject?.get("author")?.asString ?: "",
        language = json?.asJsonObject?.get("language")?.asString ?: "",
        episodeCount = json?.asJsonObject?.get("episodeCount")?.asInt ?: 0,
        subscribed = null,
        url = json?.asJsonObject?.get("url")?.asString ?: "",
        link = json?.asJsonObject?.get("link")?.asString ?: "",
        podcastIndexOrgId = json?.asJsonObject?.get("id")?.asLong ?: 0,
        podcastIndexOrgGuid = json?.asJsonObject?.get("guid")?.asString ?: ""
    )
}

data class SingleFeedResult(
    val status: String,
    val feed: Feed,
    val description: String
)

data class FeedsResult(
    val status: String,
    val feeds: List<Feed>,
    val count: Int,
    val query: String,
    val description: String
)