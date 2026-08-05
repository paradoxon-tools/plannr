# Transaction editor workflow prototype

Throwaway Compose Multiplatform prototype for [Prototype the transaction editor workflow](https://github.com/paradoxon-tools/plannr/issues/18).

Four variants retain the original editor's transaction-type navigation and focused-input model while comparing the layout of its persistent "receipt":

- **A — Original reference:** a reconstruction of the `plannr-kmm` amount, source/destination, category, and focused-input composition.
- **B — Ledger receipt:** a dense ruled summary that makes every server relationship explicit.
- **C — Route receipt:** a source-to-counterparty/destination path, with profile and timing as metadata.
- **D — Torn receipt:** a narrow typographic paper receipt optimized for rapid scanning.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\transaction-editor-compose run
```

Use the floating arrows or Left/Right keys to compare variants. Within every variant, switch Expense/Income/Transfer and One-off/Recurring. Press `O` to toggle connectivity and `S` to simulate losing connectivity during save. Press Escape to close a focused input or picker.

All mutations, relationships, recurrence previews, and connectivity states are in-memory fixtures. This prototype is intentionally disposable and has no networking, persistence, or production error handling.
