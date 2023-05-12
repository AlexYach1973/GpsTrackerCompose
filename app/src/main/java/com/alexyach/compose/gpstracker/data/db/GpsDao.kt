package com.alexyach.compose.gpstracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsDao {

    @Query("SELECT * from track")
    fun getAllItems(): Flow<List<TrackItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(item: TrackItem)

    @Update
    suspend fun update(item: TrackItem)

    @Delete
    suspend fun delete(item: TrackItem)

}