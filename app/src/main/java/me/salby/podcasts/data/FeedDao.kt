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