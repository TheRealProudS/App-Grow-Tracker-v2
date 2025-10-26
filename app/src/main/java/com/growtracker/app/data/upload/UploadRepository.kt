package com.growtracker.app.data.upload

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadRepository(private val dao: UploadDao) {

    suspend fun enqueue(
        localUri: String,
        predictionLabel: String?,
        top1Score: Float?,
        confidenceBucket: String?,
        modelVersion: String?
    ): Long = withContext(Dispatchers.IO) {
        dao.insert(
            UploadQueueEntry(
                localUri = localUri,
                createdAt = System.currentTimeMillis(),
                predictionLabel = predictionLabel,
                top1Score = top1Score,
                confidenceBucket = confidenceBucket,
                modelVersion = modelVersion
            )
        )
    }

    suspend fun nextBatch(limit: Int = 5): List<UploadQueueEntry> = withContext(Dispatchers.IO) {
        dao.nextBatch(limit)
    }

    suspend fun markUploading(id: Long) = withContext(Dispatchers.IO) {
        dao.markStatus(id, UploadStatus.UPLOADING)
    }

    suspend fun markDone(id: Long) = withContext(Dispatchers.IO) {
        dao.markStatus(id, UploadStatus.DONE)
    }

    suspend fun markFailed(id: Long, retry: Boolean) = withContext(Dispatchers.IO) {
        if (retry) dao.markRetry(id, UploadStatus.FAILED) else dao.markStatus(id, UploadStatus.FAILED)
    }
}
