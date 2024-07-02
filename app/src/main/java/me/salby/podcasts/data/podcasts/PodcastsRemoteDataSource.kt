package me.salby.podcasts.data.podcasts

import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.salby.podcasts.BuildConfig
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.EpisodeDeserializer
import me.salby.podcasts.data.podcasts.model.EpisodesResult
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.FeedDeserializer
import me.salby.podcasts.data.podcasts.model.FeedsResult
import me.salby.podcasts.data.podcasts.model.SingleFeedResult
import me.salby.podcasts.di.IoDispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject

class PodcastsRemoteDataSource @Inject constructor(
    private val podcastsService: PodcastIndexOrgApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchPodcastFeedById(
        id: Long
    ): Feed =
        withContext(ioDispatcher) {
            podcastsService
                .feed(id)
                .feed
        }

    /**
     * Fetches the feeds that match the query by title, author or owner from the network and
     * returns the result.
     */
    suspend fun fetchPodcastFeedsByQuery(
        query: String,
        max: Int = 100,
        clean: Boolean = false
    ): List<Feed> =
        withContext(ioDispatcher) {
            podcastsService
                .searchByTerm(query, max, clean)
                .feeds
        }

    suspend fun fetchEpisodesByFeed(feedId: Long): List<Episode> =
        withContext(ioDispatcher) {
            podcastsService
                .episodesByFeed(feedId)
                .episodes
        }
}

interface PodcastIndexOrgApi {
    @GET("podcasts/byfeedid")
    suspend fun feed(
        @Query("id") id: Long
    ): SingleFeedResult

    @GET("search/byterm")
    suspend fun searchByTerm(
        @Query("q") query: String,
        @Query("max") max: Int?,
        @Query("clean") clean: Boolean?
    ): FeedsResult

    @GET("episodes/byfeedid")
    suspend fun episodesByFeed(
        @Query("id") feedId: Long,
        @Query("max") max: Int = 1000
    ): EpisodesResult

    companion object {
        private const val BASE_URL = "https://api.podcastindex.org/api/1.0/"

        fun create(): PodcastIndexOrgApi {
            val converter = GsonBuilder()
                .registerTypeAdapter(Feed::class.java, FeedDeserializer())
                .registerTypeAdapter(Episode::class.java, EpisodeDeserializer())
                .create()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(converter))
                .client(createClient())
                .build()
            return retrofit.create(PodcastIndexOrgApi::class.java)
        }

        private fun createClient() =
            OkHttpClient.Builder()
                .addInterceptor(LoggingInterceptor())
                .addInterceptor(AuthInterceptor())
                .build()
    }
}

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val (time, token) = createTimeAndHash()
        val request = chain.request().newBuilder()
            .addHeader("User-Agent", "SalbyPodcasts/0.0.1")
            .addHeader("X-Auth-Key", BuildConfig.PODCASTINDEX_KEY)
            .addHeader("X-Auth-Date", "$time")
            .addHeader("Authorization", token)
            .build()
        return chain.proceed(request)
    }

    fun createTimeAndHash(): Pair<Long, String> {
        val time = System.currentTimeMillis() / 1000
        val sha1Token = MessageDigest
            .getInstance("SHA-1")
            .digest("${BuildConfig.PODCASTINDEX_KEY}${BuildConfig.PODCASTINDEX_SECRET}${time}".toByteArray())
            .joinToString(separator = "") { "%02x".format(it) }
        return time to sha1Token
    }
}

internal class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime()
        Log.d(
            "LoggingInterceptor",
            java.lang.String.format(
                "Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()
            )
        )
        val response: Response = chain.proceed(request)
        val t2 = System.nanoTime()
        Log.d(
            "LoggingInterceptor",
            java.lang.String.format(
                "Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6, response.headers()
            )
        )
        return response
    }
}