package com.example.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.example.data.model.CourierItem
import com.example.util.CryptoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ScreenCaptureManager(private val context: Context) {

    private val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    fun createProjectionIntent(): Intent? {
        return projectionManager?.createScreenCaptureIntent()
    }

    fun setMediaProjectionResult(resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            mediaProjection = projectionManager?.getMediaProjection(resultCode, data)
        }
    }

    val isProjectionReady: Boolean
        get() = mediaProjection != null

    /**
     * Captures screenshot from system MediaProjection or high-res view fallback,
     * writes PNG to local disk, computes SHA-256 hash, and constructs CourierItem.
     */
    suspend fun captureToCourierBuffer(activity: Activity? = null): CourierItem = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "fistshot_$timestamp.png"
        val storageDir = File(context.cacheDir, "courier_captures").apply { mkdirs() }
        val targetFile = File(storageDir, fileName)

        val bitmap: Bitmap = if (mediaProjection != null) {
            captureFromMediaProjection() ?: captureFromDecorView(activity) ?: captureDeviceDisplay(activity)
        } else {
            captureFromDecorView(activity) ?: captureDeviceDisplay(activity)
        }

        // Save lossless PNG
        FileOutputStream(targetFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        }

        val sha256 = CryptoUtils.sha256(targetFile)
        val fileSizeBytes = targetFile.length()

        // Generate small thumbnail to keep in memory
        val thumbnail = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width / 4,
            bitmap.height / 4,
            true
        )

        CourierItem(
            id = UUID.randomUUID().toString(),
            fileName = fileName,
            filePath = targetFile.absolutePath,
            fileSizeBytes = fileSizeBytes,
            sha256Hash = sha256,
            width = bitmap.width,
            height = bitmap.height,
            capturedAt = System.currentTimeMillis(),
            bitmap = thumbnail
        )
    }

    private fun captureFromDecorView(activity: Activity?): Bitmap? {
        if (activity == null) return null
        return try {
            val decorView = activity.window.decorView
            val width = decorView.width.takeIf { it > 0 } ?: 1080
            val height = decorView.height.takeIf { it > 0 } ?: 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            decorView.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing decorView", e)
            null
        }
    }

    private suspend fun captureFromMediaProjection(): Bitmap? = withContext(Dispatchers.Main) {
        val proj = mediaProjection ?: return@withContext null
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        val display = proj.createVirtualDisplay(
            "FistShotDisplay",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null
        )
        virtualDisplay = display

        // Wait brief delay for frame buffer
        kotlinx.coroutines.delay(200)

        var resultBitmap: Bitmap? = null
        val image = reader.acquireLatestImage()
        if (image != null) {
            try {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width

                val bmp = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bmp.copyPixelsFromBuffer(buffer)
                resultBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading image from MediaProjection", e)
            } finally {
                image.close()
            }
        }

        display?.release()
        reader.close()
        virtualDisplay = null
        imageReader = null

        resultBitmap
    }

    private fun captureDeviceDisplay(activity: Activity?): Bitmap {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        val width = if (metrics.widthPixels > 0) metrics.widthPixels else 1080
        val height = if (metrics.heightPixels > 0) metrics.heightPixels else 1920

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (activity != null) {
            try {
                activity.window.decorView.draw(canvas)
                return bitmap
            } catch (ignored: Exception) {}
        }

        canvas.drawColor(Color.WHITE)
        return bitmap
    }

    fun release() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    companion object {
        private const val TAG = "ScreenCaptureManager"
    }
}
