# Finances information-hierarchy prototype

Throwaway Compose Multiplatform prototype for [Prototype the Finances information hierarchy](https://github.com/paradoxon-tools/plannr/issues/16).

It compares three structurally different arrangements of the reviewed **Guided attention** inventory on the single, vertically scrollable Finances feature pane. The selected Ledger visual system, content boundary, and full-takeover secondary-screen behavior stay constant:

- **A — Question ladder:** answer what changes soon, what needs attention, and where money is in a predictable scan.
- **B — Account register:** lead with the complete account inventory, then explain upcoming change and attention items.
- **C — Triage briefing:** lead with action and exceptions, then reveal upcoming activity and complete accounts.

## Reviewed outcome

**A — Question ladder** is the selected hierarchy, with the reviewed order **What changes soon? → What needs attention? → Where is my money?** This brings near-term change and actionable exceptions ahead of the complete account inventory without changing the Guided-attention content policy.

Run from the repository root:

```bash
./apps/server/gradlew.bat -p apps/mobile/prototypes/finances-overview-compose run
```

Use the floating controls or Left/Right keys to compare variants. Press `T` for light/dark appearance, `F` for 100%/200% text, and `A` to switch between two attention items and the clear state. Click any labelled row to inspect the stacked secondary destination it would open; press Escape or use Back to return.

The `3 weeks behind*` fixture is deliberately provisional. [Define time-based funding health](https://github.com/paradoxon-tools/plannr/issues/26) owns the final status language and threshold.

All data is in-memory fixture data. The prototype is intentionally disposable and has no networking, persistence, or production error handling.
