package me.salby.podcasts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.salby.podcasts.data.podcasts.model.Progress
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode

@Dao
interface ProgressDao {
    @Transaction
    @Query("SELECT * FROM progress ORDER BY updated_at DESC")
    fun findAll(): Flow<List<ProgressWithEpisode>>

    @Transaction
    @Query("SELECT * FROM progress WHERE episode_id = :episodeId ORDER BY updated_at DESC")
    fun findByEpisode(episodeId: Int): Flow<ProgressWithEpisode?>

    @Insert
    suspend fun insertAll(vararg progresses: Progress): List<Long>

    @Update
    suspend fun update(progress: Progress)

    @Delete
    suspend fun delete(progress: Progress)
}