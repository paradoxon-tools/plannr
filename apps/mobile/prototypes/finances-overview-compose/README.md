# Finances overview content-inventory prototype

Throwaway Compose Multiplatform prototype for [Define the Finances overview content inventory](https://github.com/paradoxon-tools/plannr/issues/25).

It compares three materially different answers to what the single, vertically scrollable Finances pane should expose. The selected Ledger visual system, domain ownership model, and full-takeover secondary-screen behavior stay fixed:

- **A — Complete inventories:** list every account, contract, and saving goal, plus representative upcoming entries.
- **B — Attention filtered:** always list accounts, but surface only contracts and goals that need attention; complete inventories move behind section actions.
- **C — Summary directory:** keep only summaries and entry points on the overview; every entity inventory moves to a focused secondary screen.
- **D — Question led:** organize representative content around the questions the user came to answer.
- **E — Accounts as spine:** make accounts the complete inventory and nest purpose relationships beneath them.
- **F — Funding ledger:** make contracts and saving goals the complete inventory while accounts become a compact directory.
- **G — Activity preview:** expose recent and upcoming transaction entries while entities appear only as relationship context.
- **H — Action launcher:** show no inventories; make the overview a task-oriented list of entry points.
- **I — Two horizons:** divide content into current state and the next 30 days.
- **J — Unified index:** mix every primary entity into one typed, ungrouped inventory.
- **K — Guided attention:** combine B's filtering with D's user questions.
- **L — Guided accounts:** let each question resolve into E's account spine.
- **M — Account exceptions:** keep accounts complete but nest only relationships needing attention.
- **N — Selected account:** answer all overview questions for one account at a time.
- **O — Attention by account:** group funding exceptions beneath the account whose funds back them.
- **P — Answer ladder:** reduce D to a numbered sequence of concise answers and destinations.
- **Q — Account matrix:** give each account a compact now / next / attention summary.
- **R — Purpose trails:** show complete accounts followed by their connected purpose views as trails.
- **S — Browse and triage:** pair a complete account browser with a separate compact attention queue.

All nineteen intentionally keep pockets inside account, contract, and saving-goal screens. Financial-profile relationships appear as row metadata where relevant; there is no global profile selector, profile-first navigation, or profile-management surface.

Run from the repository root:

```powershell
.\apps\server\gradlew.bat -p apps\mobile\prototypes\finances-overview-compose run
```

Use the floating controls or Left/Right keys to compare variants. Press `T` for light/dark appearance and `F` for 100%/200% text. Click any labelled row to inspect the stacked secondary destination it would open; press Escape or use Back to return.

All data is in-memory fixture data. The prototype is intentionally disposable and has no networking, persistence, or production error handling.
