# Previous Plannr mobile interaction audit

## Question

Which interaction concepts, composable boundaries, and visual structures in the two previous Plannr clients are worth reimplementing, and which implementation patterns should be rejected?

## Decision

Reuse the references as an interaction vocabulary, not as a code base.

The new client should reimplement three ideas:

1. A horizontally paged application shell whose selected feature owns an expanding label in the bottom navigation.
2. A reusable, vertically revealed secondary workspace that can show transactions for a scope such as all finances, one account, or one pocket.
3. A transaction editor that keeps amount and selected entities visible while a focused input dock switches among amount, partner, category, pocket, and secondary details.

None of the custom pager, overscroll, or two-pane state machinery should be copied. The new implementation should have one authoritative state machine per interaction, use the platform Compose pager for both manual and programmatic paging, and explicitly lock top-level feature swiping whenever a feature-owned workspace is open or moving.

## Source scope

The audit used the source code in:

- `plannr-kmm` at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`
- `plannr-kmp` at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`

Both source worktrees contained pre-existing local changes. Of the files cited below, only `plannr-kmm`'s `TransactionEditor.kt` and `TransactionEditorViewModel.kt` were modified; their relevant behavior is cited as the inspected working-tree version. No reference repository was changed by this audit.

## What to reimplement

### 1. Feature descriptors separated from the pager chrome

The older pager has a useful boundary: page metadata (route, icon, label, main-target status) and page content are declared separately from the scaffold, while a connection object allows the pager and expanding navigation to be placed independently.[^kmm-pager-contract][^kmm-pager-configuration] The newer experiment improves the call site by deriving its four pages from navigation-owned child instances rather than embedding feature state in the pager.[^kmp-home]

Reimplement this as immutable feature descriptors with a stable feature ID:

```text
FeatureDescriptor(id, shortLabel, icon, placeholder/available)
FeaturePagerScaffold(selectedFeatureId, features, pagingEnabled, onSelect)
```

The descriptor should contain navigation metadata only. Feature state, loading, and child navigation belong to the feature component. The shell should accept placeholder features so Dashboard, Chat, Groceries, and Finances can exercise the complete pager from the beginning.

### 2. Expanding-label navigation driven by pager progress

Both experiments reserve fixed icon space and assign the remaining width to the selected label, transferring that width and the highlight color between source and target as a page moves.[^kmm-expanding-label][^kmp-expanding-label] This is the distinctive behavior worth preserving.

Reimplement the visual transformation from the authoritative pager's current page and offset. Keep short labels, but make the layout degrade safely when the available label width is zero. Text clipping, font scaling, right-to-left layout, and a reduced-motion mode need explicit prototype coverage. A tab must remain a normal semantic button with its label as the content description.

### 3. A scoped vertical transaction workspace

The newer experiment expresses a strong product concept: the Finances overview is a normal scrollable pane; overscrolling at its bottom reveals a full-height transaction pane; reversing from the transaction pane's top returns to the overview. The same transition is available through an explicit floating action and back control.[^kmp-finances-host][^kmp-transaction-overview]

Reimplement this as a generic `VerticalWorkspaceHost`, not as a Finances-specific pager. Its content should be parameterized by scope:

```text
TransactionScope.All
TransactionScope.Account(accountId)
TransactionScope.Pocket(pocketId)
TransactionScope.Contract(contractId)
```

This lets the Finances home, account details, and pocket details share the same behavior while issuing different transaction queries. The workspace must always have explicit controls in addition to the edge gesture, so discoverability and accessibility do not depend on overscroll.

The reusable state should be small and explicit:

```text
Settled(Overview | Workspace)
Dragging(from, to, offsetPx)
Settling(from, to, target)
```

Pane size is layout input, not initialization that launches work. A single `Animatable` (or equivalent state holder) should settle the transition. Feature data and list state stay outside this transition state.

### 4. A focused transaction-editor input dock

The older editor has a coherent information hierarchy:

- Expense, income, and transfer are sibling transaction types.
- Amount and currently selected entities remain visible in the upper summary.
- An input-mode row shows completion and switches a lower dock among calculator, partner, category, and pocket selectors.
- Date and notes are secondary details rather than competing with the primary fields.[^kmm-editor-shell][^kmm-editor-frame]

The view model also demonstrates useful domain behavior: transfers need source and target pockets, while expense/income require amount, pocket, partner, and category; suggestions can react to earlier selections.[^kmm-editor-state]

Reimplement those behaviors around one immutable `TransactionDraft` and one `TransactionEditorState` exposed to Compose. Make transaction type an ordinary selector rather than another custom pager. Make field focus an event-driven state transition; do not auto-advance by delayed coroutines. Validation and `isSaveEnabled` should be derived from the draft, and Save must submit a snapshot of the same visible type and fields.

Suggested composable boundaries:

```text
TransactionEditorRoute(state, onEvent)
TransactionTypeSelector(type, onSelect)
TransactionSummary(draft, activeField, onFieldSelect)
TransactionInputDock(activeField, suggestions, onEvent)
TransactionSecondaryDetails(date, notes, recurrence, onEvent)
```

### 5. A sectioned, single-scroll Finances overview

The newer experiment validates a sectioned overview containing account and contract summaries, with cards opening focused details rather than allocating more top-level navigation destinations.[^kmp-finances-sections] Shared-bound transitions between a card and its detail screen are a useful optional polish because the stable entity ID already provides a natural transition key.[^kmp-account-card]

Keep the section/card/detail structure, not the current visuals or exact grid. The new overview should be one keyed lazy layout. Avoid placing an unbounded eager grid or a long repeated column inside one lazy item; the existing account and contract grids eagerly iterate every entity, and the budgets placeholder puts 25 rows in one item.[^kmp-finances-sections][^kmp-grid-sections]

## Gesture ownership contract

The reported horizontal/vertical conflict is structural, not a tuning problem. In the newer experiment the top-level `HorizontalPager` is always user-scrollable, while the Finances pane independently installs a vertical nested-scroll connection around two full-screen panes.[^kmp-pager-scaffold][^kmp-two-pane-layout] No shared policy tells the parent when its child has entered a focused workspace.

Adopt this contract:

1. **Root overview owns horizontal feature paging.** A feature may vertically scroll its content, and normal Compose touch-slop/axis locking decides between those orthogonal gestures.
2. **A vertical workspace may start only at the active list's relevant boundary and only in the direction of the other pane.** It consumes the unhandled vertical delta after the child reaches that boundary.
3. **Once a workspace drag starts, the feature owns the gesture until cancel or settle.** The shell disables direct horizontal paging from the first non-zero workspace offset.
4. **A settled secondary workspace keeps horizontal paging disabled.** It exits by reverse edge gesture, Back/Up, or an explicit workspace action.
5. **Feature-tab taps never move a half-open workspace sideways.** If global navigation remains visible, a tap first returns the current feature to a settled overview and then changes the feature. During the settling animation it is temporarily disabled.
6. **Dialogs, editors, and full-screen detail routes own all underlying gestures.** The shell pager is disabled while they are active.

Expose this through feature-owned interaction state rather than by reaching into a child's list:

```text
FeatureInteraction.Root
FeatureInteraction.WorkspaceDragging
FeatureInteraction.WorkspaceOpen(scope)
FeatureInteraction.Overlay
```

The shell derives `userScrollEnabled` from `FeatureInteraction.Root`. This policy should be tested with diagonal drags, interrupted drags, flings at both list boundaries, system Back, tab taps during settling, and iOS back-swipe areas.

## What to reject

### Competing pager state machines

Both pager implementations replace `HorizontalPager` with two manually offset page compositions for programmatic transitions, then force the underlying pager to the destination at the end.[^kmm-manual-pager][^kmp-pager-scaffold] This creates two sources of position truth, changes the composition tree between manual and programmatic movement, and makes page state/lifecycle behavior depend on how navigation was initiated.

The newer `NavigationPagerState` adds a graph of `snapshotFlow`, `combine`, and mutable flows to infer static, manual, and programmatic states around `PagerState`.[^kmp-pager-state] The Home screen then synchronizes that state bidirectionally with Decompose in two effects; when the selected Decompose page changes while `currentPage` still reports the old page, the reverse effect can select the old page again.[^kmp-home-sync]

Use one `PagerState`; call its supported animation operation for tab selection; observe its settled page once to update navigation state. Never render a different page composition path for programmatic navigation.

### The current two-pane implementation

`TwoPaneLayout` calls `pagerState.initialize(...)` directly during composition.[^kmp-two-pane-layout] Every call launches two new, unscoped collectors, so recomposition can multiply transition observers and animation jobs.[^kmp-two-pane-state-init] The state also stores container dimensions in uninitialized delegated properties, remembers injected scroll states without keys, reads child `canScrollForward`/`canScrollBackward` in pre-scroll, and consumes the entire fling without using its velocity to choose or shape the settle.[^kmp-two-pane-state]

Retain the interaction, discard this implementation. Initialization must be idempotent and side-effect-free; effects must be tied to composition/lifecycle exactly once; layout changes must update size without launching collectors.

### The custom overscroll modifier

The unused `customOverscroll` modifier launches a coroutine for individual scroll callbacks, captures callback/orientation values in unkeyed `remember`/`LaunchedEffect`, catches every exception during decay, and reports broad velocity consumption.[^kmp-custom-overscroll] It can reorder high-frequency input updates and is unnecessary for the desired two-state workspace.

Do not copy it. Build the workspace on one nested-scroll connection with synchronous delta accounting and one controlled settle animation.

### State writes during composition and measurement caches

The editor's `InputPanel` assigns remembered state during composition, subcomposes each mode to cache its height, and keys that cache only by input mode.[^kmm-input-panel] The cache becomes stale when width, orientation, font scale, locale, or suggestion content changes. Entity summary composables also write their “previously selected” state directly during composition.[^kmm-editor-entity-animation]

Retain the focused-dock transition, but use normal animated-content/size primitives and keyed state. Prefer draw/placement transforms for per-frame motion rather than remeasurement or state mutation during composition.

### Fragmented mutable state and delayed focus changes

The editor reads many independent flows in each of three page compositions, while Save chooses behavior from a separate mutable transaction-type flow.[^kmm-editor-frame][^kmm-editor-state] Input selection launches delayed jobs without cancelling a previous auto-advance, so a stale job can change focus after the user has already acted.[^kmm-editor-delayed-focus] The Save callback runs when the coroutine completes normally even if the `Either` contains a domain error, which can close the editor without a successful dispatch.[^kmm-editor-save]

Use one state snapshot, serialized events, cancellation-aware focus changes, and an explicit submit result (`Idle`, `Submitting`, `Succeeded`, `Failed`). Connectivity is a Save precondition for the first release.

### Eager hidden composition and unkeyed collections

The current two-pane layout composes both full-screen panes at all times.[^kmp-two-pane-layout] The transaction list does not provide item keys, and section grids eagerly iterate their entire collections.[^kmp-transaction-list][^kmp-grid-sections] This is acceptable as a visual experiment, not as the performance model for the new client.

The prototype should measure both alternatives: retaining both pane compositions for continuity versus lifecycle-gating expensive collection/rendering in the hidden pane. Production lists need stable keys and content types; off-screen placeholder features should not keep expensive collectors active merely because the shell can page to them.

### Reference visuals and inaccessible affordances

The references hard-code the previous color language in cards and controls and frequently use empty icon descriptions.[^kmp-account-card][^kmp-finances-host] The new reversible black/white system supersedes these visuals. Motion, shared bounds, expanding labels, and spatial hierarchy may carry forward; colors, typography, icon treatment, and unlabeled controls may not.

## Prototype acceptance checks

The shell/workspace prototype should prove:

- manual swiping and tab selection end in the same page state and retain page-local state;
- a Finances workspace cannot be dragged horizontally into another feature;
- all-Finances, account, and pocket transaction scopes use the same transition host;
- Back and explicit controls work at every transition point;
- interrupted and diagonal gestures settle deterministically;
- light/dark inversion, large text, reduced motion, Android gesture navigation, and iOS edge gestures remain usable;
- hidden panes do not continue expensive rendering or data collection without an explicit retention reason.

The editor prototype should prove:

- type changes produce a coherent draft and validation state;
- transfer source/target selection cannot silently collapse into the same pocket;
- every field is reachable without relying on animation or auto-advance;
- Save cannot activate while offline, invalid, or already submitting;
- a failed request leaves the editor and visible draft intact for manual retry during that open editor session.

## Source references

[^kmm-pager-contract]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/components/layout/pager/NavigationPager.kt`, lines 64-104 and 149-239, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmm-pager-configuration]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/components/layout/pager/PagerConfiguration.kt`, lines 14-20 and 62-143, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmp-home]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/home/HomeScreen.kt`, lines 34-40 and 58-104, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmm-expanding-label]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/components/layout/pager/NavigationPager.kt`, lines 443-552, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmp-expanding-label]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/layout/pager/NavigationPagerTabContainer.kt`, lines 94-197 and 200-263, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-finances-host]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/FinancesHomePageContent.kt`, lines 118-185 and 188-254, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-transaction-overview]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/transactions/TransactionOverviewComponent.kt`, lines 126-195, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmm-editor-shell]: Inspected working-tree version of `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/screen/transaction/editor/TransactionEditor.kt`, lines 66-175 and 177-224, based on `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmm-editor-frame]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/screen/transaction/editor/TransactionEditorPanel.kt`, lines 77-229 and 232-409, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmm-editor-state]: Inspected working-tree version of `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/domain/viewmodel/transaction/editor/TransactionEditorViewModel.kt`, lines 193-252 and 351-447, based on `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmp-finances-sections]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/FinancesHomePageContent.kt`, lines 188-254, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-account-card]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/accounts/thumbnail/AccountThumbnail.kt`, lines 22-75, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-grid-sections]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/accounts/overview/Content.kt`, lines 25-62; and `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/contracts/ContractOverviewComponent.kt`, lines 200-238, both at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-pager-scaffold]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/layout/pager/NavigationPagerScaffold.kt`, lines 18-49 and 53-95, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-two-pane-layout]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/layout/twopane/TwoPaneLayout.kt`, lines 24-82, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmm-manual-pager]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/components/layout/pager/NavigationPager.kt`, lines 364-439, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmp-pager-state]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/layout/pager/NavigationPagerState.kt`, lines 82-229, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-home-sync]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/home/HomeScreen.kt`, lines 42-56 and 71-82, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-two-pane-state-init]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/layout/twopane/TwoPanePagerState.kt`, lines 152-231, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-two-pane-state]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/layout/twopane/TwoPanePagerState.kt`, lines 43-67, 88-150, and 235-280, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmp-custom-overscroll]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/components/customOverscroll.kt`, lines 33-184, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
[^kmm-input-panel]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/screen/transaction/editor/InputPanel.kt`, lines 29-152, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmm-editor-entity-animation]: `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/ui/screen/transaction/editor/TransactionEditorPanel.kt`, lines 412-545, at `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmm-editor-delayed-focus]: Inspected working-tree version of `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/domain/viewmodel/transaction/editor/TransactionEditorViewModel.kt`, lines 125-140, based on `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmm-editor-save]: Inspected working-tree version of `plannr-kmm/bridge/src/commonMain/kotlin/de/chennemann/plannr/bridge/domain/viewmodel/transaction/editor/TransactionEditorViewModel.kt`, lines 146-175, based on `9b6b16a58a30c2817e98783638a25ca4c89e5d3c`.
[^kmp-transaction-list]: `plannr-kmp/client/compose/src/commonMain/kotlin/de/chennemann/plannr/ui/screen/finances/transactions/TransactionOverviewComponent.kt`, lines 183-194, at `3d4a489fafec726c807e8b0cfdcb6edbe58cf4fc`.
