package me.salby.podcasts.data.podcasts

import me.salby.podcasts.data.podcasts.model.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.FeedWithEpisodes
import me.salby.podcasts.data.podcasts.model.Progress
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode
import java.time.LocalDateTime
import javax.inject.Inject

class PodcastsRepository @Inject constructor(
    private val podcastsLocalDataSource: PodcastsLocalDataSource,
    private val podcastsRemoteDataSource: PodcastsRemoteDataSource,
    private val externalScope: CoroutineScope
) {
    private val getFeedMutex = Mutex()
    private val getFeed: MutableMap<Long, Feed> = mutableMapOf()

    private val searchFeedsResultMutex = Mutex()
    private val searchFeedsResult: MutableMap<String, List<Feed>> = mutableMapOf()

    private val episodesMutex = Mutex()
    private val episodes: MutableMap<Long, List<Episode>> = mutableMapOf()

    fun observeFeedByIdWithEpisodes(id: Int): Flow<FeedWithEpisodes?> = podcastsLocalDataSource
        .observeFeedByIdWithEpisodes(id)

    fun observeSubscriptions(): Flow<List<Feed>> =
        podcastsLocalDataSource.observeSubscriptions()

    fun observeFeeds(): Flow<List<Feed>> =
        podcastsLocalDataSource.observeFeeds()

    suspend fun getLocalFeedById(id: Int): Feed? {
        return podcastsLocalDataSource.fetchPodcastFeedById(id)
    }

    /**
     * Returns the podcast with the given [id].
     */
    suspend fun getFeedById(id: Long, refresh: Boolean = false): Feed? {
        if (!refresh && getFeed.containsKey(id)) {
            return getFeedMutex.withLock { getFeed[id] }
        }

        return withContext(externalScope.coroutineContext) {
            podcastsLocalDataSource.fetchPodcastByIndexId(id)?.let {
                getFeedMutex.withLock { getFeed[id] = it }
                return@withContext it
            }

            val result = podcastsRemoteDataSource.fetchPodcastFeedById(id)
            getFeedMutex.withLock { getFeed[id] = result }
            result
        }
    }

    suspend fun searchPodcastFeeds(query: String, refresh: Boolean = false): List<Feed> {
        if (!refresh && searchFeedsResult.containsKey(query)) {
            return searchFeedsResultMutex.withLock { searchFeedsResult[query] ?: emptyList() }
        }

        return withContext(externalScope.coroutineContext) {
            val result = podcastsRemoteDataSource.fetchPodcastFeedsByQuery(query)
            searchFeedsResultMutex.withLock { searchFeedsResult[query] = result }
            result
        }
    }

    suspend fun getEpisodesByFeed(feedId: Long, refresh: Boolean = false): List<Episode> {
        if (!refresh && episodes.containsKey(feedId)) {
            return episodesMutex.withLock { episodes[feedId] ?: emptyList() }
        }

        return withContext(externalScope.coroutineContext) {
            val result = podcastsRemoteDataSource.fetchEpisodesByFeed(feedId)
            episodesMutex.withLock { episodes[feedId] = result }
            result
        }
    }

    fun observeProgressForAllEpisodes(): Flow<List<ProgressWithEpisode>> =
        podcastsLocalDataSource.observeProgress()

    suspend fun updateProgress(progressMs: Long, episode: Episode) =
        updateProgress(progressMs, episode.id)

    suspend fun updateProgress(progressMs: Long, episodeId: Int) {
        val (progress, _) = podcastsLocalDataSource.observeProgressByEpisode(episodeId).first() ?: let {
            val now = LocalDateTime.now()
            podcastsLocalDataSource.insertProgress(
                Progress(
                    progress = progressMs,
                    createdAt = now,
                    updatedAt = now,
                    episodeId = episodeId
                )
            )
            podcastsLocalDataSource.observeProgressByEpisode(episodeId).first()!!
        }
        updateProgress(progress.copy(progress = progressMs))
    }

    suspend fun updateProgress(progress: Progress) =
        podcastsLocalDataSource.updateProgress(progress.copy(updatedAt = LocalDateTime.now()))

    suspend fun getEpisodeById(id: Int): Episode? =
        podcastsLocalDataSource.observeEpisodeById(id).first()

    suspend fun subscribeToFeed(feed: Feed) =
        podcastsLocalDataSource
            .updateFeed(feed.copy(subscribed = LocalDateTime.now()))

    suspend fun unsubscribeFromFeed(feed: Feed) =
        podcastsLocalDataSource
            .updateFeed(feed.copy(subscribed = null))

    suspend fun insertFeeds(vararg feed: Feed): List<Long> =
        podcastsLocalDataSource
            .insertFeeds(*feed)

    suspend fun insertEpisodes(vararg episode: Episode): List<Long> =
        podcastsLocalDataSource
            .insertEpisodes(*episode)
}