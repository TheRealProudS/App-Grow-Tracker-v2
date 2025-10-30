package com.growtracker.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrUtils {
    private const val DISCORD_FALLBACK = "https://discord.gg/s8qyyWZV4W"

    /**
     * Build an Android Intent URI that prefers opening the custom scheme growtracker://p/{id}
     * and falls back to a Discord invite if the app is not installed.
     * Example:
     * intent://p/PLNT-123#Intent;scheme=growtracker;package=com.growtracker.app;S.browser_fallback_url=https://discord.gg/...;end
     */
    fun buildPlantQrPayload(plantId: String, packageName: String): String {
        val safeId = plantId.trim()
        return "intent://p/$safeId#Intent;scheme=growtracker;package=$packageName;S.browser_fallback_url=$DISCORD_FALLBACK;end"
    }

    /** Generate a QR code bitmap with ECC-Q and small margin. */
    fun generateQrBitmap(content: String, sizePx: Int = 1024): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
            EncodeHintType.MARGIN to 1
        )
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            val offset = y * sizePx
            for (x in 0 until sizePx) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888)
    }

    /** Save a bitmap as PNG into Downloads (or Pictures) via MediaStore; returns Uri on success. */
    fun saveBitmapToDownloads(context: Context, bitmap: Bitmap, fileName: String): android.net.Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, contentValues) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            Toast.makeText(context, "QR-Code gespeichert: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Cleanup on failure
            runCatching { resolver.delete(uri, null, null) }
            Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
            return null
        }
        return uri
    }
}
