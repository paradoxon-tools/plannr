# Reversible monochrome visual-system prototype

Throwaway Compose Multiplatform prototype for [Prototype the reversible monochrome visual system](https://github.com/paradoxon-tools/plannr/issues/15).

It compares three deliberately different visual-system treatments around the same fixture data. The Finances content and section order are test material only; this prototype does not decide the information architecture.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\visual-system-compose run
```

Use the floating controls or Left/Right keys to compare:

- **A — Ledger:** quiet canvas, ruled groups, square geometry, monochrome-first actions, and identity color as a small marker.
- **B — Tonal:** layered neutral surfaces, soft geometry, a single cool interaction accent, and bounded semantic/entity color.
- **C — Signal:** hard black/white hierarchy, framed modules, monospace numeric emphasis, and color confined to labelled signals.

Each variant can be reviewed in light and dark appearances, with normal or 200% text, and with full or reduced motion. Press `T`, `M`, or `F` to toggle those settings.

The prototype is intentionally disposable and has no persistence, networking, or production error handling.
