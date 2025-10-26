package com.growtracker.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import com.growtracker.app.BuildConfig

object SecurityUtils {
    private fun masterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun writeEncryptedBytes(context: Context, outFile: File, bytes: ByteArray) {
        if (!outFile.parentFile!!.exists()) outFile.parentFile!!.mkdirs()
        val encFile = EncryptedFile.Builder(
            context,
            outFile,
            masterKey(context),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        encFile.openFileOutput().use { it.write(bytes) }
    }

    fun readDecryptedBytes(context: Context, inFile: File): ByteArray {
        val encFile = EncryptedFile.Builder(
            context,
            inFile,
            masterKey(context),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        return encFile.openFileInput().use { it.readBytes() }
    }

    fun writeEncryptedText(context: Context, outFile: File, text: String) {
        writeEncryptedBytes(context, outFile, text.toByteArray(Charsets.UTF_8))
    }

    fun readDecryptedText(context: Context, inFile: File): String {
        return readDecryptedBytes(context, inFile).toString(Charsets.UTF_8)
    }

    /** Compute SHA-256 of the first signing certificate; returns hex lowercase. */
    fun getSigningCertSha256(context: Context): String? {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
                val cert = info.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray() ?: return null
                sha256Hex(cert)
            } else {
                @Suppress("DEPRECATION")
                val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION") val cert = info.signatures?.firstOrNull()?.toByteArray() ?: return null
                sha256Hex(cert)
            }
        } catch (e: Exception) { null }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { ((it.toInt() and 0xFF).toString(16)).padStart(2, '0') }
    }

    /** Verify the app signing cert against BuildConfig.SIGNATURE_SHA256; returns true if matches or if expected is empty. */
    fun verifyAppSignature(context: Context): Boolean {
        val expected = BuildConfig.SIGNATURE_SHA256?.trim().orEmpty()
        if (expected.isEmpty()) return true // disabled until configured
        val actual = getSigningCertSha256(context)?.lowercase(Locale.US) ?: return false
        val normalizedExpected = expected.lowercase(Locale.US)
        // Support providing either hex or base64 pin string (starts with sha256/)
        return if (normalizedExpected.startsWith("sha256/")) {
            // Not directly comparable here; callers should set hex for app signature by preference
            false
        } else {
            actual == normalizedExpected
        }
    }
}
