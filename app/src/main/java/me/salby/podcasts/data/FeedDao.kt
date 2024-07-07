package me.salby.podcasts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.FeedWithEpisodes

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds")
    fun findAll(): Flow<List<Feed>>

    @Transaction
    @Query("SELECT * FROM feeds")
    fun findAllWithEpisodes(): Flow<List<FeedWithEpisodes>>

    @Query("SELECT * FROM feeds WHERE subscribed IS NOT NULL ORDER BY subscribed DESC")
    fun findAllSubscribed(): Flow<List<Feed>>

    @Query("SELECT * FROM feeds WHERE id = :id LIMIT 1")
    fun findById(id: Int): Flow<Feed?>

    @Transaction
    @Query("SELECT * FROM feeds WHERE id = :id LIMIT 1")
    fun findByIdWithEpisodes(id: Int): Flow<FeedWithEpisodes?>

    @Query("SELECT * FROM feeds WHERE podcast_index_org_id = :id LIMIT 1")
    fun findByPodcastIndexOrgId(id: Long): Flow<Feed?>

    @Insert
    suspend fun insertAll(vararg feeds: Feed): List<Long>

    @Update
    suspend fun update(feed: Feed)

    @Delete
    suspend fun delete(feed: Feed)
}

val PreviewFeeds = listOf(
    Feed(
        id = 1,
        title = "r8Dio - Undskyld vi roder",
        url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
        link = "https://www.r8dio.dk/",
        description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
        author = "r8Dio",
        image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
        language = "da",
        isExplicit = false,
        episodeCount = 211,
        subscribed = null
    ),
    Feed(
        id = 1,
        title = "70mmr",
        url = "https://anchor.fm/s/12d1fabc/podcast/rss",
        link = "https://www.70mmpod.com",
        description = "A podcast for movie fans, inspired by Letterboxd. (We're not experts.) Each week artist Danny Haas, spiritual advisor Protolexus, and journeyman podcaster Slim discuss a recently watched film together. A brand new theme each month. Their love for each other cannot be broken. Or can it? Merch available + new episodes every Monday.",
        author = "70mm",
        image = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/3057511/3057511-1648820616219-0adef97444f1.jpg",
        language = "en",
        isExplicit = false,
        episodeCount = 255,
        subscribed = null
    )
)