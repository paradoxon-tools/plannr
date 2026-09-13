# Mobile Prototype Inspiration

This document keeps the durable decisions and useful prompts from the discarded mobile prototype work so the baseline app can borrow from them without carrying the prototype code in the repository.

The prototype implementations under `apps/mobile/prototypes` are intentionally disposable and should not be treated as production foundations. The authoritative record for each decision remains the linked GitHub issue.

## Carry forward

### Application shell

- [Prototype the multi-feature application shell and gesture ownership](https://github.com/paradoxon-tools/plannr/issues/14): use one authoritative horizontal pager across Dashboard, Chat, Groceries, and Finances.
- The expanding-label feature navigation should be derived from pager progress rather than maintained as a second pager state machine.
- Stacked secondary screens use **full takeover**: they hide feature navigation, lock root paging, own Back/Up, and return to the same originating pane and local state on dismissal.

### Transaction workspaces

- [Define reusable scoped transaction workspaces](https://github.com/paradoxon-tools/plannr/issues/17): use a **boundary handoff** from a Finances main surface into its vertically adjacent transaction feed.
- The root pager remains available until the handoff actually begins.
- Once the handoff begins, the transaction workspace owns gestures and the root pager locks.
- The transaction feed keeps an immutable originating scope key until it closes.
- Back/Up and a downward gesture from the feed's upper boundary return to the same originating surface with local state preserved.

### Finances overview

- [Define the Finances overview content inventory](https://github.com/paradoxon-tools/plannr/issues/25): the overview is a guided-attention surface, not an exhaustive dashboard.
- Organize it around three questions:
  `What changes soon?`
  `What needs attention?`
  `Where is my money?`
- Always show the all-account **Upcoming summary**.
- Show only contracts and saving goals that need attention; healthy rows should not consume overview space.
- Always show the full account inventory with **account balance** and **available balance**.
- Do not show a single all-account financial-position total.
- Pockets are not peer-level overview items; they belong inside focused account, contract, and saving-goal views.

### Hierarchy and scanning

- [Prototype the Finances information hierarchy](https://github.com/paradoxon-tools/plannr/issues/16): use the **Question ladder** ordering.
- The vertical scan order is:
  `What changes soon?`
  `What needs attention?`
  `Where is my money?`
- The overview should prioritize near-term change and actionable exceptions before complete inventory.

### Visual direction

- [Prototype the reversible monochrome visual system](https://github.com/paradoxon-tools/plannr/issues/15): use the **Ledger** direction as the baseline visual language.
- Keep independently tuned light and dark appearances with the same semantic roles, not simple inversion.
- Prefer strong typography, ruled grouping, compact density, minimal radii, and sparse use of raised surfaces.
- Keep primary actions monochrome.
- Reserve accent and semantic color for genuine interaction state, identity markers, and status meaning.
- Use financial amounts with stronger numeric emphasis than surrounding labels.

### Transaction editor

- [Prototype the transaction editor workflow](https://github.com/paradoxon-tools/plannr/issues/18): keep the strongest `plannr-kmm` interaction vocabulary.
- The editor should retain a persistent summary and one focused input surface at a time.
- Use the **Route receipt** concept:
  amount and transaction type as headline,
  source to counterparty or destination as a readable route,
  profile and timing as supporting metadata.
- Hide fields that do not apply instead of disabling them.
- Editing remains connectivity-gated; no draft or outbox promise should be implied.

## Open question worth revisiting

- [Keep feature navigation accessible from boundary transaction feeds](https://github.com/paradoxon-tools/plannr/issues/29) was not resolved.
- If the baseline app later needs quick cross-feature exits from an active scoped feed, revisit the three explored directions:
  persistent rail,
  reveal drawer,
  header switch.
- That follow-up should preserve the already-resolved constraints from the shell and boundary-handoff decisions.

## Where to look if needed

- The issue comments above contain the chosen decisions and links to the prototype branches and commits that produced them.
- If a future implementation needs inspiration, read the issue resolution first and consult the linked prototype branch only as supporting context.
