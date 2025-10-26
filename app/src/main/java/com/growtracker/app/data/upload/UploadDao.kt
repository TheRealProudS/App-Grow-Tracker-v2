package com.growtracker.app.data.upload

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UploadQueueEntry): Long

    @Update
    suspend fun update(entry: UploadQueueEntry)

    @Query("SELECT * FROM upload_queue WHERE status IN ('PENDING','FAILED') ORDER BY createdAt ASC LIMIT :limit")
    suspend fun nextBatch(limit: Int): List<UploadQueueEntry>

    @Query("SELECT COUNT(*) FROM upload_queue WHERE status='PENDING'")
    fun pendingCountFlow(): Flow<Int>

    @Query("DELETE FROM upload_queue WHERE status='DONE' AND createdAt < :threshold")
    suspend fun purgeOldDone(threshold: Long): Int

    @Query("UPDATE upload_queue SET status=:status, retries=retries+1 WHERE id=:id")
    suspend fun markRetry(id: Long, status: UploadStatus)

    @Query("UPDATE upload_queue SET status=:status WHERE id=:id")
    suspend fun markStatus(id: Long, status: UploadStatus)
}
