# Transaction editor workflow prototype

Throwaway Compose Multiplatform prototype for [Prototype the transaction editor workflow](https://github.com/paradoxon-tools/plannr/issues/18).

Three variants answer how one shared editor should expose one-off and recurring income, expense, and transfer entries:

- **A — Focus deck:** a persistent transaction summary above one focused input surface.
- **B — Guided path:** one relationship or schedule decision per step, with a live receipt alongside it.
- **C — Compact sheet:** a scan-friendly form whose relationship rows open full-screen pickers.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\transaction-editor-compose run
```

Use the floating arrows or Left/Right keys to compare variants. Within every variant, switch Expense/Income/Transfer and One-off/Recurring. Press `O` to toggle connectivity and `S` to simulate losing connectivity during save. Press Escape to close a focused input or picker.

All mutations, relationships, recurrence previews, and connectivity states are in-memory fixtures. This prototype is intentionally disposable and has no networking, persistence, or production error handling.
