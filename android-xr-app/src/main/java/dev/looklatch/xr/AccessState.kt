package dev.looklatch.xr

enum class AccessState(
    val title: String,
    val summary: String,
    val safetyNote: String,
) {
    Idle(
        title = "Idle",
        summary = "対象はまだ選択されていません。",
        safetyNote = "視線、近接、確認のすべてを待っています。",
    ),
    TargetDetected(
        title = "Target detected",
        summary = "XR app がロック対象を検出しました。",
        safetyNote = "検出だけでは解除しません。",
    ),
    Armed(
        title = "Armed",
        summary = "ユーザーの intent が保持され、解除待機になりました。",
        safetyNote = "arm は unlock ではありません。phone proximity を待っています。",
    ),
    ProximityVerified(
        title = "Proximity verified",
        summary = "Android phone companion が近接を検証しました。",
        safetyNote = "近接だけでも解除しません。物理確認が必要です。",
    ),
    PhysicalConfirmationRequired(
        title = "Physical confirmation required",
        summary = "タッチ、タップ、NFC、QR などの明示確認を待っています。",
        safetyNote = "この確認が成立するまで simulated unlock は発生しません。",
    ),
    Unlocked(
        title = "Simulated unlocked",
        summary = "simulated lock endpoint が解除済み表示になりました。",
        safetyNote = "これはデモ用の状態表示で、実物の鍵には接続していません。",
    ),
    Expired(
        title = "Expired",
        summary = "視線逸脱、timeout、またはキャンセルで失効しました。",
        safetyNote = "失効後は Idle からやり直します。",
    ),
    Error(
        title = "Error",
        summary = "近接失敗またはセッション不整合が発生しました。",
        safetyNote = "エラー時は安全側に倒して解除しません。",
    ),
}

fun AccessState.nextDemoState(): AccessState = when (this) {
    AccessState.Idle -> AccessState.TargetDetected
    AccessState.TargetDetected -> AccessState.Armed
    AccessState.Armed -> AccessState.ProximityVerified
    AccessState.ProximityVerified -> AccessState.PhysicalConfirmationRequired
    AccessState.PhysicalConfirmationRequired -> AccessState.Unlocked
    AccessState.Unlocked,
    AccessState.Expired,
    AccessState.Error -> AccessState.Idle
}
