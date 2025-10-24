package com.growtracker.app.ui.ai

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.BitmapFactory
import kotlin.math.roundToInt
import androidx.camera.core.ImageProxy
import java.util.concurrent.atomic.AtomicReference

/**
 * Convert an [ImageProxy] (YUV_420_888 or JPEG) to an ARGB_8888 [Bitmap].
 *
 * Implementation details:
 *  - Handles per-plane row & pixel strides for YUV_420_888.
 *  - Performs standard BT.601 YUV -> RGB conversion.
 *  - Clamps channels to 0..255.
 *  - Reuses buffers via a simple static pool to reduce allocations during repeated captures.
 *  - Applies rotation based on imageInfo.rotationDegrees (0,90,180,270).
 */
fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val image = imageProxy.image ?: throw IllegalArgumentException("ImageProxy has no image")
    return when (image.format) {
        ImageFormat.YUV_420_888 -> yuv420ToBitmap(imageProxy)
        ImageFormat.JPEG -> jpegToBitmap(imageProxy)
        else -> throw IllegalArgumentException("Unsupported image format: ${image.format} (${formatName(image.format)})")
    }.let { rotateBitmapIfNeeded(it, imageProxy.imageInfo.rotationDegrees) }
}

private fun jpegToBitmap(imageProxy: ImageProxy): Bitmap {
    val buffer = imageProxy.planes.firstOrNull()?.buffer
        ?: throw IllegalArgumentException("JPEG plane buffer missing")
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalArgumentException("Failed to decode JPEG bytes")
}

private fun yuv420ToBitmap(imageProxy: ImageProxy): Bitmap {
    val image = imageProxy.image ?: throw IllegalArgumentException("ImageProxy has no image")

    val width = imageProxy.width
    val height = imageProxy.height

    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    val yRowStride = yPlane.rowStride
    val yPixelStride = yPlane.pixelStride // usually 1
    val uRowStride = uPlane.rowStride
    val uPixelStride = uPlane.pixelStride
    val vRowStride = vPlane.rowStride
    val vPixelStride = vPlane.pixelStride

    val out = LeafSenseBufferPool.obtainIntArray(width * height)
    var outIndex = 0

    for (row in 0 until height) {
        val yRowOffset = row * yRowStride
        // Chroma rows are subsampled by 2
        val chromaRow = row / 2
        val uRowOffset = chromaRow * uRowStride
        val vRowOffset = chromaRow * vRowStride
        for (col in 0 until width) {
            val yCol = yRowOffset + col * yPixelStride
            val chromaCol = (col / 2) * uPixelStride

            val yValue = (yBuffer.get(yCol).toInt() and 0xFF)
            val uValue = (uBuffer.get(uRowOffset + chromaCol).toInt() and 0xFF) - 128
            val vValue = (vBuffer.get(vRowOffset + chromaCol).toInt() and 0xFF) - 128

            // BT.601 conversion
            val yFloat = yValue.toFloat()
            var r = (yFloat + 1.402f * vValue).toInt()
            var g = (yFloat - 0.344136f * uValue - 0.714136f * vValue).toInt()
            var b = (yFloat + 1.772f * uValue).toInt()
            if (r < 0) r = 0 else if (r > 255) r = 255
            if (g < 0) g = 0 else if (g > 255) g = 255
            if (b < 0) b = 0 else if (b > 255) b = 255

            out[outIndex++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    val bmp = Bitmap.createBitmap(out, width, height, Bitmap.Config.ARGB_8888)
    LeafSenseBufferPool.recycle(out)
    return bmp
}

/**
 * Downscaled variant to reduce CPU cost and memory bandwidth when full resolution is not required.
 * The algorithm samples every Nth pixel based on a computed scale so that the larger dimension
 * does not exceed [maxDimension]. Simple point sampling (nearest) is used for speed.
 */
fun imageProxyToBitmapScaled(imageProxy: ImageProxy, maxDimension: Int): Bitmap {
    val image = imageProxy.image ?: throw IllegalArgumentException("ImageProxy has no image")
    if (image.format == ImageFormat.JPEG) {
        val full = jpegToBitmap(imageProxy)
        val scale = (maxDimension.toFloat() / maxOf(full.width, full.height)).coerceAtMost(1f)
        val w = (full.width * scale).roundToInt().coerceAtLeast(1)
        val h = (full.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = if (scale < 1f) Bitmap.createScaledBitmap(full, w, h, true) else full
        return rotateBitmapIfNeeded(scaled, imageProxy.imageInfo.rotationDegrees)
    }
    if (image.format != ImageFormat.YUV_420_888) {
        throw IllegalArgumentException("Unsupported image format: ${image.format} (${formatName(image.format)})")
    }
    val srcW = imageProxy.width
    val srcH = imageProxy.height
    val scale = if (srcW >= srcH) maxDimension.toFloat() / srcW else maxDimension.toFloat() / srcH
    val scaleClamped = if (scale >= 1f) 1f else scale
    val dstW = (srcW * scaleClamped).roundToInt().coerceAtLeast(1)
    val dstH = (srcH * scaleClamped).roundToInt().coerceAtLeast(1)

    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    val yRowStride = yPlane.rowStride
    val yPixelStride = yPlane.pixelStride
    val uRowStride = uPlane.rowStride
    val uPixelStride = uPlane.pixelStride
    val vRowStride = vPlane.rowStride
    val vPixelStride = vPlane.pixelStride

    val out = LeafSenseBufferPool.obtainIntArray(dstW * dstH)
    var outIndex = 0

    val xRatio = srcW.toFloat() / dstW
    val yRatio = srcH.toFloat() / dstH

    var dstY = 0
    while (dstY < dstH) {
        val srcY = (dstY * yRatio).toInt()
        val yRowOffset = srcY * yRowStride
        val chromaRow = srcY / 2
        val uRowOffset = chromaRow * uRowStride
        val vRowOffset = chromaRow * vRowStride
        var dstX = 0
        while (dstX < dstW) {
            val srcX = (dstX * xRatio).toInt()
            val yCol = yRowOffset + srcX * yPixelStride
            val chromaCol = (srcX / 2) * uPixelStride
            val yValue = (yBuffer.get(yCol).toInt() and 0xFF)
            val uValue = (uBuffer.get(uRowOffset + chromaCol).toInt() and 0xFF) - 128
            val vValue = (vBuffer.get(vRowOffset + chromaCol).toInt() and 0xFF) - 128

            val yFloat = yValue.toFloat()
            var r = (yFloat + 1.402f * vValue).toInt()
            var g = (yFloat - 0.344136f * uValue - 0.714136f * vValue).toInt()
            var b = (yFloat + 1.772f * uValue).toInt()
            if (r < 0) r = 0 else if (r > 255) r = 255
            if (g < 0) g = 0 else if (g > 255) g = 255
            if (b < 0) b = 0 else if (b > 255) b = 255
            out[outIndex++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            dstX++
        }
        dstY++
    }
    val bmp = Bitmap.createBitmap(out, dstW, dstH, Bitmap.Config.ARGB_8888)
    LeafSenseBufferPool.recycle(out)
    return rotateBitmapIfNeeded(bmp, imageProxy.imageInfo.rotationDegrees)
}

/**
 * Rotate the bitmap according to the rotation degrees reported by CameraX (0, 90, 180, 270).
 * If rotationDegrees == 0 the original instance is returned.
 * NOTE: Front camera mirroring (if added later) would need an additional horizontal flip.
 */
fun rotateBitmapIfNeeded(src: Bitmap, rotationDegrees: Int): Bitmap {
    if (rotationDegrees == 0) return src
    val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}

/**
 * Simple object managing reusable IntArray buffers sized by capacity.
 * Thread-safety: single AtomicReference; good enough for low contention (UI capture rate).
 * If size requirement differs, a new array is allocated and stored if larger than current.
 */
private object LeafSenseBufferPool {
    private val intArrayRef = AtomicReference<IntArray?>(null)
    fun obtainIntArray(minSize: Int): IntArray {
        val cur = intArrayRef.get()
        return if (cur == null || cur.size < minSize) {
            val arr = IntArray(minSize)
            intArrayRef.set(arr)
            arr
        } else cur
    }
    fun recycle(@Suppress("UNUSED_PARAMETER") array: IntArray) { /* kept for symmetry */ }
}

/** Map numeric ImageFormat constants to readable names for diagnostics/logging. */
private fun formatName(format: Int): String = when (format) {
    ImageFormat.YUV_420_888 -> "YUV_420_888"
    ImageFormat.JPEG -> "JPEG"
    ImageFormat.NV21 -> "NV21"
    ImageFormat.RAW_SENSOR -> "RAW_SENSOR"
    ImageFormat.DEPTH16 -> "DEPTH16"
    ImageFormat.DEPTH_POINT_CLOUD -> "DEPTH_POINT_CLOUD"
    else -> "UNKNOWN($format)"
}
