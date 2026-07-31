# Scoped transaction workspace prototype

Throwaway Compose Multiplatform prototype for deciding how a Finances main pane moves into and back out of a server-scoped transaction feed.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\shell-compose run
```

The UI lives in `commonMain`; the desktop entry point is only a convenient Windows review host. It extends the Compose artifact captured by **Prototype the multi-feature application shell and gesture ownership** and preserves that artifact's one authoritative Compose Foundation `HorizontalPager` / `PagerState`.

Use the floating bottom switcher or Left/Right keys to compare:

- **A — Boundary handoff:** after a scoped main surface reaches its end, continued upward movement hands off to a vertically adjacent transaction feed. The root pager locks as soon as that boundary transition starts; swiping down at the feed's top returns.
- **B — In-content gateway:** a Transactions section explicitly replaces the main pane. List overscroll never navigates. The root pager locks at activation and the feature navigation hides while the feed is open.
- **C — Persistent dock:** a scope-aware gateway remains anchored above the shell. It opens a vertical replacement without coupling navigation to content scroll position; root paging locks at activation.

In every variant, opening a fixture Account, Pocket, or Contract detail first captures that server scope and applies the already-decided full-takeover rule for focused details. The transaction feed keeps that scope until it closes. The visible finance data and content order are fixtures, not proposals.

The state panel exposes the current surface, server scope, root-pager availability, and gesture owner. Use Escape or the visible Back action to close the transaction workspace, then Escape or **Finances** to close a scoped detail.
