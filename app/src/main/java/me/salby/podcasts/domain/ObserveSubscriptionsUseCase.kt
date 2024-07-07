package me.salby.podcasts.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import me.salby.podcasts.data.podcasts.PodcastsRepository
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.di.IoDispatcher
import javax.inject.Inject

class ObserveSubscriptionsUseCase @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<List<Feed>> =
        podcastsRepository
            .observeSubscriptions()
            .flowOn(ioDispatcher)
}