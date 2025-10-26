package com.growtracker.app.work

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.NetworkType
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.growtracker.app.data.upload.*
import com.growtracker.app.data.consent.DataUploadConsentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import com.growtracker.app.security.PlayIntegrityClient

class ImageUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val db by lazy { AppDatabase.get(appContext) }
    private val repo by lazy { UploadRepository(db.uploadDao()) }
    private val consentRepo by lazy { DataUploadConsentRepository(appContext) }

    override suspend fun doWork(): Result {
        // Verify app signature if configured; bail out if mismatch
        if (!com.growtracker.app.security.SecurityUtils.verifyAppSignature(applicationContext)) {
            return Result.failure()
        }
        val consent = consentRepo.consentFlow.first()
        if (!consent) return Result.success()

        val baseUrl = inputData.getString(KEY_BASE_URL) ?: DEFAULT_BASE_URL
        val api = UploadNetworkModule.api(applicationContext, baseUrl)

        val batch = repo.nextBatch(BATCH_SIZE)
        if (batch.isEmpty()) return Result.success()

        for (entry in batch) {
            // Mark uploading
            repo.markUploading(entry.id)
            val success = uploadSingle(api, entry)
            if (success) {
                repo.markDone(entry.id)
            } else {
                repo.markFailed(entry.id, retry = true)
            }
        }
        // If some failed, we can retry later (Result.retry), but to avoid tight loops use exponential backoff via schedule
        val anyFailed = batch.any { it.status != UploadStatus.DONE }
        return if (anyFailed) Result.retry() else Result.success()
    }

    private suspend fun uploadSingle(api: IngestApi, entry: UploadQueueEntry): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(entry.localUri)
            val file = if (uri.scheme == "file") File(uri.path!!) else File(uri.path ?: return@withContext false)
            if (!file.exists()) return@withContext false

            val imagePart = MultipartBody.Part.createFormData(
                name = "image",
                filename = file.name,
                body = file.asRequestBody("image/jpeg".toMediaType())
            )
            val metaJson = JSONObject().apply {
                put("predictionLabel", entry.predictionLabel)
                put("top1Score", entry.top1Score)
                put("confidenceBucket", entry.confidenceBucket)
                put("modelVersion", entry.modelVersion)
                put("createdAt", entry.createdAt)
            }.toString().toRequestBody("application/json".toMediaTypeOrNull())

            // Fetch a Play Integrity token (best with server-provided nonce; here we use a random placeholder)
            val nonce = PlayIntegrityClient.generateNonce()
            val integrityToken = PlayIntegrityClient.requestTokenOrNull(applicationContext, nonce)

            val resp = api.uploadImage(integrityToken, imagePart, metaJson)
            resp.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val UNIQUE_NAME = "image_upload_worker"
        private const val BATCH_SIZE = 5
        private const val DEFAULT_BASE_URL = "https://api.example.com" // TODO config
        const val KEY_BASE_URL = "base_url"

        fun schedule(context: Context, baseUrl: String = DEFAULT_BASE_URL) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work = PeriodicWorkRequestBuilder<ImageUploadWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag(UNIQUE_NAME)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
        }
    }
}
