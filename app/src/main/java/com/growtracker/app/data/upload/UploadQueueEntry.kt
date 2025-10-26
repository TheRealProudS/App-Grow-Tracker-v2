package com.growtracker.app.data.upload

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_queue")
data class UploadQueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localUri: String,            // content:// or file path
    val createdAt: Long,             // epoch millis
    val predictionLabel: String?,
    val top1Score: Float?,
    val confidenceBucket: String?,   // e.g. HIGH / MID / LOW
    val modelVersion: String?,
    val retries: Int = 0,
    val status: UploadStatus = UploadStatus.PENDING
)

enum class UploadStatus { PENDING, UPLOADING, FAILED, DONE }
