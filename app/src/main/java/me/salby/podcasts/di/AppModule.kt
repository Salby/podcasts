package me.salby.podcasts.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import me.salby.podcasts.data.podcasts.PodcastIndexOrgApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.salby.podcasts.data.PodcastsDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providePodcastsDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(
            app, PodcastsDatabase::class.java, "podcasts_database"
        ).build()

    @Singleton
    @Provides
    fun provideFeedDao(db: PodcastsDatabase) =
        db.feedDao()

    @Singleton
    @Provides
    fun provideEpisodeDao(db: PodcastsDatabase) =
        db.episodeDao()

    @Singleton
    @Provides
    fun provideProgressDao(db: PodcastsDatabase) =
        db.progressDao()

    @Singleton
    @Provides
    fun providePodcastIndexOrgApi() =
        PodcastIndexOrgApi.create()

    @Provides
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}