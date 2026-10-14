package com.example.gesture

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.model.GestureState
import com.example.data.model.GestureTriggerEvent
import com.example.data.model.HandPose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class GestureDetectorEngine : ImageAnalysis.Analyzer {

    private val _gestureState = MutableStateFlow(GestureState())
    val gestureState: StateFlow<GestureState> = _gestureState.asStateFlow()

    private var consecutiveFistFrames = 0
    private var consecutiveOpenFrames = 0
    private var lastStablePose = HandPose.IDLE

    // Sensitivity thresholds
    var minSkinPixelThreshold = 0.08f // At least 8% of ROI is skin
    var fistSolidityThreshold = 0.62f // Compactness of fist

    override fun analyze(image: ImageProxy) {
        try {
            if (image.format == ImageFormat.YUV_420_888 && image.planes.size >= 3) {
                processYuvImage(image)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing camera frame", e)
        } finally {
            image.close()
        }
    }

    private fun processYuvImage(image: ImageProxy) {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val width = image.width
        val height = image.height

        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        // Sample central 60% ROI on a downsampled grid (step = 8)
        val startX = (width * 0.2f).toInt()
        val endX = (width * 0.8f).toInt()
        val startY = (height * 0.2f).toInt()
        val endY = (height * 0.8f).toInt()
        val step = 8

        var totalSampled = 0
        var skinPixels = 0

        var minX = endX
        var maxX = startX
        var minY = endY
        var maxY = startY

        for (y in startY until endY step step) {
            for (x in startX until endX step step) {
                totalSampled++

                val yIndex = y * yRowStride + x * yPixelStride
                val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride

                if (yIndex < yBuffer.remaining() && uvIndex < uBuffer.remaining() && uvIndex < vBuffer.remaining()) {
                    val yVal = yBuffer.get(yIndex).toInt() and 0xFF
                    val uVal = uBuffer.get(uvIndex).toInt() and 0xFF
                    val vVal = vBuffer.get(uvIndex).toInt() and 0xFF

                    // Standard human skin color range in YCbCr:
                    // Y in [60..245], Cb (u) in [77..127], Cr (v) in [133..175]
                    val isSkin = yVal in 60..245 && uVal in 75..135 && vVal in 130..180
                    if (isSkin) {
                        skinPixels++
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }
        }

        val skinDensity = if (totalSampled > 0) skinPixels.toFloat() / totalSampled else 0f
        val boundWidth = max(1, maxX - minX)
        val boundHeight = max(1, maxY - minY)
        val boundingArea = (boundWidth * boundHeight) / (step * step)
        val solidity = if (boundingArea > 0) min(1f, skinPixels.toFloat() / boundingArea) else 0f

        classifyPose(skinDensity, solidity)
    }

    private fun classifyPose(skinDensity: Float, solidity: Float) {
        val detectedPose: HandPose
        val confidence: Float
        val statusMessage: String

        if (skinDensity < minSkinPixelThreshold) {
            detectedPose = HandPose.IDLE
            confidence = 0f
            statusMessage = "Hold hand in front of camera"
            consecutiveFistFrames = 0
            consecutiveOpenFrames = 0
        } else {
            // Hand is present
            if (solidity >= fistSolidityThreshold) {
                // Dense compact blob -> Closed Fist
                detectedPose = HandPose.FIST_CLOSED
                confidence = min(0.98f, 0.6f + (solidity - fistSolidityThreshold) * 2f)
                statusMessage = "Fist detected (Screen Capture)"
                consecutiveFistFrames++
                consecutiveOpenFrames = 0
            } else {
                // Extended fingers / lower solidity -> Open Palm
                detectedPose = HandPose.OPEN_PALM
                confidence = min(0.95f, 0.5f + skinDensity * 1.5f)
                statusMessage = "Open Palm detected (Ready to grab/release)"
                consecutiveOpenFrames++
                consecutiveFistFrames = 0
            }
        }

        var trigger = GestureTriggerEvent.NONE

        // Detect OPEN_PALM -> FIST_CLOSED transition (Grab/Capture trigger)
        if (detectedPose == HandPose.FIST_CLOSED && consecutiveFistFrames == 2) {
            if (lastStablePose == HandPose.OPEN_PALM) {
                trigger = GestureTriggerEvent.CAPTURE_SCREEN_TRIGGERED
            }
            lastStablePose = HandPose.FIST_CLOSED
        }

        // Detect FIST_CLOSED -> OPEN_PALM transition (Release/Beam trigger)
        if (detectedPose == HandPose.OPEN_PALM && consecutiveOpenFrames == 2) {
            if (lastStablePose == HandPose.FIST_CLOSED) {
                trigger = GestureTriggerEvent.RELEASE_TRANSFER_TRIGGERED
            }
            lastStablePose = HandPose.OPEN_PALM
        }

        if (detectedPose == HandPose.IDLE && consecutiveFistFrames == 0 && consecutiveOpenFrames == 0) {
            // After being away, reset stable pose to idle
            lastStablePose = HandPose.IDLE
        }

        _gestureState.value = GestureState(
            currentPose = detectedPose,
            previousPose = lastStablePose,
            confidence = confidence,
            triggerEvent = trigger,
            statusFeedback = statusMessage,
            isCameraActive = true,
            skinPixelDensity = skinDensity,
            contourSolidity = solidity
        )
    }

    /**
     * Simulation method: triggers an Open Hand -> Closed Fist capture transition.
     */
    fun simulateFistCapture() {
        lastStablePose = HandPose.OPEN_PALM
        _gestureState.value = GestureState(
            currentPose = HandPose.FIST_CLOSED,
            previousPose = HandPose.OPEN_PALM,
            confidence = 0.99f,
            triggerEvent = GestureTriggerEvent.CAPTURE_SCREEN_TRIGGERED,
            statusFeedback = "Simulated Fist: Grab screenshot!",
            isCameraActive = _gestureState.value.isCameraActive,
            skinPixelDensity = 0.25f,
            contourSolidity = 0.85f
        )
    }

    /**
     * Simulation method: triggers a Closed Fist -> Open Hand release transition.
     */
    fun simulateOpenHandRelease() {
        lastStablePose = HandPose.FIST_CLOSED
        _gestureState.value = GestureState(
            currentPose = HandPose.OPEN_PALM,
            previousPose = HandPose.FIST_CLOSED,
            confidence = 0.98f,
            triggerEvent = GestureTriggerEvent.RELEASE_TRANSFER_TRIGGERED,
            statusFeedback = "Simulated Open Hand: Release & Beam!",
            isCameraActive = _gestureState.value.isCameraActive,
            skinPixelDensity = 0.22f,
            contourSolidity = 0.45f
        )
    }

    fun consumeTriggerEvent() {
        _gestureState.value = _gestureState.value.copy(triggerEvent = GestureTriggerEvent.NONE)
    }

    companion object {
        private const val TAG = "GestureDetectorEngine"
    }
}
