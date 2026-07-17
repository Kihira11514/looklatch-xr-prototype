package dev.looklatch.phone.proximity

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

class FakeProximityProvider : ProximityProvider {
    private val mutableEvents = MutableSharedFlow<ProximityEvent>(replay = 1)
    private var activeSessionId: String = "no-session"
    private var activeTarget: ProximityTarget? = null

    override val events: SharedFlow<ProximityEvent> = mutableEvents.asSharedFlow()

    override suspend fun startSession(target: ProximityTarget) {
        activeSessionId = UUID.randomUUID().toString()
        activeTarget = target
        emit(ProximityConfidence.Nearby, "Fake session started. User appears near the target.")
    }

    override suspend fun stopSession() {
        emit(ProximityConfidence.Unknown, "Fake session stopped.")
        activeSessionId = "no-session"
        activeTarget = null
    }

    suspend fun simulate(confidence: ProximityConfidence, detail: String) {
        emit(confidence, detail)
    }

    private suspend fun emit(confidence: ProximityConfidence, detail: String) {
        mutableEvents.emit(
            ProximityEvent(
                sessionId = activeSessionId,
                target = activeTarget,
                confidence = confidence,
                detail = detail,
            ),
        )
    }
}
