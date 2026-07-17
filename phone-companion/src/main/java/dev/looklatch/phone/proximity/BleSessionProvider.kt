package dev.looklatch.phone.proximity

interface BleSessionProvider {
    suspend fun openSession(target: ProximityTarget): BleSession

    suspend fun closeSession(sessionId: String)
}

data class BleSession(
    val id: String,
    val targetId: String,
    val status: BleSessionStatus,
)

enum class BleSessionStatus {
    Placeholder,
    Opening,
    Ready,
    Closed,
    Failed,
}

class PlaceholderBleSessionProvider : BleSessionProvider {
    override suspend fun openSession(target: ProximityTarget): BleSession {
        return BleSession(
            id = "ble-placeholder",
            targetId = target.id,
            status = BleSessionStatus.Placeholder,
        )
    }

    override suspend fun closeSession(sessionId: String) = Unit
}
