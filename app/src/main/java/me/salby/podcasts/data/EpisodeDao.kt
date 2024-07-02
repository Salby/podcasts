package me.salby.podcasts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import me.salby.podcasts.data.podcasts.model.Episode

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes ORDER BY published DESC")
    fun findAll(): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE feed_id = :feedId ORDER BY published DESC")
    fun findAllByFeedId(feedId: Int): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    fun findById(id: Int): Flow<Episode?>

    @Insert
    suspend fun insertAll(vararg episodes: Episode): List<Long>

    @Update
    suspend fun update(episode: Episode)

    @Delete
    suspend fun delete(episode: Episode)
}