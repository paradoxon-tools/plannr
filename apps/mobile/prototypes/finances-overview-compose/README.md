# Finances information-hierarchy prototype

Throwaway Compose Multiplatform prototype for [Prototype the Finances information hierarchy](https://github.com/paradoxon-tools/plannr/issues/16).

It compares three structurally different versions of the single, vertically scrollable Finances feature pane while holding the selected Ledger visual system and full-takeover secondary-screen behavior constant:

- **A — Position first:** present position, near-term obligations, then the structures behind them.
- **B — Time first:** a 30-day cash-flow story, followed by the entities affected by it.
- **C — Structure first:** accounts and their pockets first, then commitments, goals, profiles, and activity.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\finances-overview-compose run
```

Use the floating controls or Left/Right keys to compare variants. Press `T` for light/dark appearance and `F` for 100%/200% text. Click any labelled row to inspect the stacked secondary destination it would open; press Escape or use Back to return.

All data is in-memory fixture data. The prototype is intentionally disposable and has no networking, persistence, or production error handling.
