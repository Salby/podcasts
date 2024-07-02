package me.salby.podcasts.data.podcasts.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "progress", indices = [Index("episode_id", unique = true)])
data class Progress(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true)
    val id: Int = 0,

    val progress: Long,

    @ColumnInfo("created_at")
    val createdAt: LocalDateTime,

    @ColumnInfo("updated_at")
    val updatedAt: LocalDateTime,

    @ColumnInfo("episode_id")
    val episodeId: Int
)
