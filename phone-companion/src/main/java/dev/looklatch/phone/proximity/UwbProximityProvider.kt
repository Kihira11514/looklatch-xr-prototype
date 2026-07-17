package dev.looklatch.phone.proximity

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UwbProximityProvider : ProximityProvider {
    private val mutableEvents = MutableSharedFlow<ProximityEvent>(replay = 1)

    override val events: SharedFlow<ProximityEvent> = mutableEvents.asSharedFlow()

    override suspend fun startSession(target: ProximityTarget) {
        mutableEvents.emit(
            ProximityEvent(
                sessionId = "uwb-placeholder",
                target = target,
                confidence = ProximityConfidence.Unknown,
                detail = "UWB/Ranging integration is intentionally not implemented in this prototype.",
            ),
        )
    }

    override suspend fun stopSession() {
        mutableEvents.emit(
            ProximityEvent(
                sessionId = "uwb-placeholder",
                target = null,
                confidence = ProximityConfidence.Unknown,
                detail = "UWB placeholder session stopped.",
            ),
        )
    }
}
