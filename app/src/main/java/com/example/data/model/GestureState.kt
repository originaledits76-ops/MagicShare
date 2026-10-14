package com.example.data.model

enum class HandPose {
    IDLE,
    HAND_DETECTED,
    OPEN_PALM,
    FIST_CLOSED
}

enum class GestureTriggerEvent {
    NONE,
    CAPTURE_SCREEN_TRIGGERED, // Transition from OPEN_PALM -> FIST_CLOSED
    RELEASE_TRANSFER_TRIGGERED // Transition from FIST_CLOSED -> OPEN_PALM
}

data class GestureState(
    val currentPose: HandPose = HandPose.IDLE,
    val previousPose: HandPose = HandPose.IDLE,
    val confidence: Float = 0f,
    val triggerEvent: GestureTriggerEvent = GestureTriggerEvent.NONE,
    val statusFeedback: String = "Place hand in frame",
    val isCameraActive: Boolean = false,
    val isFrontCamera: Boolean = true,
    val skinPixelDensity: Float = 0f,
    val contourSolidity: Float = 0f
)
