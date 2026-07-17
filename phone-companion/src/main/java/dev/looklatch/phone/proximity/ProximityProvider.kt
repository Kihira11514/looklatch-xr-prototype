package dev.looklatch.phone.proximity

import kotlinx.coroutines.flow.Flow

interface ProximityProvider {
    val events: Flow<ProximityEvent>

    suspend fun startSession(target: ProximityTarget)

    suspend fun stopSession()
}

data class ProximityTarget(
    val id: String,
    val label: String,
)

data class ProximityEvent(
    val sessionId: String,
    val target: ProximityTarget?,
    val confidence: ProximityConfidence,
    val detail: String,
    val timestampMillis: Long = System.currentTimeMillis(),
)

enum class ProximityConfidence {
    Unknown,
    OutOfRange,
    Nearby,
    Verified,
    Failed,
}
