package me.salby.podcasts.data.podcasts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.salby.podcasts.data.EpisodeDao
import me.salby.podcasts.data.FeedDao
import me.salby.podcasts.data.ProgressDao
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.FeedWithEpisodes
import me.salby.podcasts.data.podcasts.model.Progress
import javax.inject.Inject

class PodcastsLocalDataSource @Inject constructor(
    private val feedDao: FeedDao,
    private val episodeDao: EpisodeDao,
    private val progressDao: ProgressDao
) {
    fun observeFeedByIdWithEpisodes(id: Int): Flow<FeedWithEpisodes?> = feedDao
        .findByIdWithEpisodes(id)
        .distinctUntilChanged()

    fun observeFeeds(): Flow<List<Feed>> = feedDao
        .findAll()
        .distinctUntilChanged()

    suspend fun fetchPodcastFeedById(id: Int): Feed? = feedDao
        .findById(id)
        .distinctUntilChanged()
        .first()

    suspend fun fetchPodcastByIndexId(podcastIndexOrgId: Long): Feed? = feedDao
        .findByPodcastIndexOrgId(podcastIndexOrgId)
        .distinctUntilChanged()
        .first()

    suspend fun fetchPodcastFeedWithEpisodesById(id: Int): FeedWithEpisodes? = feedDao
        .findByIdWithEpisodes(id)
        .distinctUntilChanged()
        .first()

    fun observeSubscriptions(): Flow<List<Feed>> = feedDao
        .findAllSubscribed()
        .distinctUntilChanged()

    fun observeEpisodeById(id: Int): Flow<Episode?> = episodeDao
        .findById(id)
        .distinctUntilChanged()

    suspend fun insertFeeds(vararg feed: Feed): List<Long> =
        feedDao.insertAll(*feed)

    suspend fun updateFeed(feed: Feed) =
        feedDao.update(feed)

    suspend fun insertEpisodes(vararg episode: Episode): List<Long> =
        episodeDao.insertAll(*episode)

    suspend fun updateEpisode(episode: Episode) =
        episodeDao.update(episode)

    fun observeProgress() = progressDao
        .findAll()
        .distinctUntilChanged()

    fun observeProgressByEpisode(episodeId: Int) =
        progressDao.findByEpisode(episodeId)

    suspend fun insertProgress(vararg progresses: Progress) =
        progressDao.insertAll(*progresses)

    suspend fun updateProgress(progress: Progress) =
        progressDao.update(progress)
}