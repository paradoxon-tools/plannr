# Compose shell prototype

Throwaway Compose Multiplatform prototype for comparing secondary-destination behavior around the already-chosen `plannr-kmp` feature pager.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\shell-compose run
```

The UI lives in `commonMain`; the desktop entry point is only a convenient review host. The shell uses Compose Foundation's `HorizontalPager` and one authoritative `PagerState`, with expanding bottom labels derived directly from pager progress as in `D:\Development\chennemann\plannr-kmp`.

Use the top prototype switcher or Left/Right keys to compare:

- **A — Full takeover:** a secondary destination covers the shell and hides feature navigation.
- **B — Feature stack:** a secondary destination replaces only the active feature pane and leaves disabled feature navigation visible.
- **C — Focused sheet:** a secondary destination overlays the shell as a modal sheet.

In every variant, swipe paging and feature selection are disabled while the secondary destination is open. Use Escape or the visible Back action to return.
