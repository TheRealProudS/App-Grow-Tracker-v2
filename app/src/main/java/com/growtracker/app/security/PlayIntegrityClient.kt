package com.growtracker.app.security

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PlayIntegrityClient {
    /** Generate a random 32-byte nonce as placeholder. For production, prefer a server-generated, bound nonce. */
    fun generateNonce(bytes: Int = 32): ByteArray {
        val b = ByteArray(bytes)
        SecureRandom().nextBytes(b)
        return b
    }

    /** Request an integrity token. Returns null if Play Services or API unavailable. */
    suspend fun requestTokenOrNull(context: Context, nonce: ByteArray): String? {
        return try {
            val im = IntegrityManagerFactory.create(context)
            val request = IntegrityTokenRequest.builder()
                .setNonce(Base64.encodeToString(nonce, Base64.NO_WRAP))
                .build()
            suspendCancellableCoroutine { cont ->
                val task = im.requestIntegrityToken(request)
                task.addOnSuccessListener { result -> cont.resume(result.token()) }
                    .addOnFailureListener { e -> cont.resume(null) }
                cont.invokeOnCancellation { /* no-op */ }
            }
        } catch (e: Exception) {
            null
        }
    }
}
