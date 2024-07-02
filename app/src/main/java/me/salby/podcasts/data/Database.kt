package me.salby.podcasts.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.Progress
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Database(
    version = 4,
    entities = [Feed::class, Episode::class, Progress::class],
    exportSchema = true
)
@TypeConverters(DurationConverter::class, DateConverter::class)
abstract class PodcastsDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun progressDao(): ProgressDao
}

class DurationConverter {
    @TypeConverter
    fun durationToSeconds(value: Duration?): Long? =
        value?.inWholeSeconds

    @TypeConverter
    fun secondsToDuration(value: Long?): Duration? =
        value?.seconds
}

class DateConverter {
    @TypeConverter
    fun localDateTimeToEpochSeconds(value: LocalDateTime?): Long? =
        value?.toEpochSecond(ZoneOffset.UTC)

    @TypeConverter
    fun epochSecondsToLocalDateTime(value: Long?): LocalDateTime? =
        value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
}