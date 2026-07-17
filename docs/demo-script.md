# Demo Script

## 目的

LookLatch XR は「見つめるだけで解除」ではなく、「見て intent を作る、スマホで近接を証明する、最後に触って確認する」体験を見せるプロトタイプです。デモでは simulated lock endpoint だけを使い、実物の鍵や車両には接続しません。

## 3 分デモ

1. ダッシュボードを開く。
   - lock state が `LOCKED` であることを見せる。
   - event log が空、または reset 直後であることを確認する。

2. XR app stub の `TargetDetected` を表示する。
   - 「ユーザーが対象を見たので、対象を検出しました」と説明する。
   - この時点ではまだ解除されないことを強調する。

3. `Armed` に進める。
   - 「視線は intent を作るだけです。ここでも lock は変わりません」と説明する。

4. phone companion の fake proximity を `Verified` にする。
   - 「スマートフォンが近接を証明しました」と説明する。
   - それでもまだ unlock されないことを見せる。

5. `PhysicalConfirmationRequired` を表示する。
   - 「ここで初めて、触る/タップする/NFC などの明示確認を求めます」と説明する。

6. `physical_confirmed` を送る。
   - endpoint の state が `SIMULATED_UNLOCKED` へ変わることを見せる。
   - 「これは simulated unlock で、実物の鍵には接続していません」と言う。

7. reset する。
   - `Idle` と `LOCKED` に戻ることを見せる。

## 強調する一言

LookLatch XR は、Android XR を現実世界の access intent layer にする実験です。視線だけでは何も解除せず、phone proximity と physical confirmation を組み合わせて、安全側に倒した体験を検証します。

## 想定 Q&A

### 本当に車を開けますか?

いいえ。このリポジトリは simulated lock endpoint のみです。実車両、実スマートロック、決済 API には接続しません。

### UWB は実装済みですか?

現時点では placeholder です。`ProximityProvider` の口だけを作り、最初は `FakeProximityProvider` で state machine と UX を検証します。

### 視線だけで誤解除しませんか?

この設計ではしません。視線は `TargetDetected` と `Armed` までで、`PhysicalConfirmationRequired` の明示確認を通らない限り simulated unlock に到達しません。
