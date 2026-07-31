# Supported KMP and Compose Multiplatform foundation

Research snapshot: 2026-07-31

Question: What current, officially supported Kotlin Multiplatform and Compose Multiplatform capabilities and constraints should bound an Android-phone and iPhone application that shares its UI, navigation behavior, presentation logic, API client, and cache while retaining narrow platform adapters?

## Answer

Plannr can use a production-supported, shared-UI foundation today. The recommended boundary is:

- a Kotlin Multiplatform library containing the application shell, Compose UI and resources, navigation model, presentation state, domain logic, API contracts, repositories, and read cache;
- a separate, thin Android application module;
- a thin Xcode iOS application that hosts the shared root composable;
- small `androidMain` and `iosMain` adapters for HTTP engines, filesystem/database construction, platform network policy, system services, and state-restoration hooks.

This is an architectural recommendation, not a claim that every API behaves identically. Kotlin Multiplatform and Compose Multiplatform both classify Android and iOS as stable platforms, but platform entry points, build hosts, networking policy, filesystem paths, app restoration, accessibility verification, and performance tooling remain platform concerns ([platform stability](https://kotlinlang.org/docs/multiplatform/supported-platforms.html), [platform-specific behavior](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)).

Use official AndroidX/JetBrains multiplatform libraries where their current stable artifacts meet the need: Compose UI/Foundation, Navigation 3 plus Navigation Event, Lifecycle/ViewModel/SavedState, Room, and DataStore. Use Ktor with `OkHttp` on Android and `Darwin` on iOS for the shared HTTP client. Keep Material 3 behind Plannr's own design-system API because the Material 3 artifact aligned with the current stable Compose Multiplatform release is still alpha.

## Supported baseline

### Versions and targets - sourced facts

At this snapshot, Kotlin 2.4.10 is the current stable Kotlin version. Its supported compatibility range is Gradle 7.6.3-9.5.0, Android Gradle Plugin 8.5.2-9.1.0, and Xcode 26.4. The exact build tuple must be selected from and kept inside the official table rather than upgrading one tool independently ([KMP compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)).

Compose Multiplatform 1.11.1 supports Android 5.0/API 21 and iOS 14, on 64-bit platforms. It requires the Compose compiler plugin whose version matches the Kotlin plugin; its native targets require at least Kotlin 2.3.10 ([Compose compatibility](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html), [Compose 1.11.1 release](https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html), [Compose compiler setup](https://kotlinlang.org/docs/multiplatform/compose-compiler.html)).

There is a practical difference between the Compose floor and Kotlin/Native's default. Kotlin/Native currently defaults Apple targets to iOS 15; `iosArm64` and `iosSimulatorArm64` are Tier 1, while building final Apple binaries requires macOS. Compose Multiplatform 1.11.1 removed `iosX64`, so an Apple-silicon Mac is the supported local iOS simulator host for this stack ([Kotlin/Native targets and hosts](https://kotlinlang.org/docs/native-target-support.html), [Compose 1.11.1 iOS changes](https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html#changes-to-ios-target-support)).

### Baseline choice - architectural inference

- Start with Kotlin 2.4.10 and Compose Multiplatform 1.11.1, pinned in a version catalog and Gradle wrapper.
- Target `iosArm64` and `iosSimulatorArm64`, with iOS 15 as the product minimum. Supporting iOS 14 is technically possible through an explicit Kotlin/Native override, but adds a configuration exception with no stated Plannr requirement.
- Set Android `minSdk` from the owner's actual device needs, with API 21 as the framework floor. The framework floor should not be mistaken for a product requirement to support a decade of Android versions.
- Use release artifacts only in production modules. Do not make an EAP repository part of the normal build.
- Record the selected Kotlin/Compose/AGP/Gradle/Xcode tuple together and upgrade it as one tested unit.

The current Compose Multiplatform 1.11.1 dependency table is mixed-stability: UI/Foundation are stable, Navigation 3 is 1.1.2, while the aligned Material 3 artifact is `1.11.0-alpha07` and Lifecycle is `2.11.0-beta01` ([1.11.1 dependency table](https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html#dependencies)). This does not make the Compose platform unstable, but it does mean Plannr should select satellite-library versions deliberately. The stable Lifecycle/ViewModel 2.10.0 examples remain documented for common code ([multiplatform ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html)).

## Project and sharing boundary

### Sourced facts

Google's current `com.android.kotlin.multiplatform.library` plugin is the supported way to add Android to a KMP library. It does not replace `com.android.application`; the Android application must be a separate module. The KMP library plugin has one variant and disables host/device tests by default unless they are explicitly enabled ([Android KMP plugin](https://developer.android.com/kotlin/multiplatform/plugin)).

Every platform still needs its own entry point. Android uses an Activity; iOS initializes a native application and hosts Compose through a `UIViewController`. JetBrains supports embedding `ComposeUIViewController` in SwiftUI or UIKit. The iOS host must include `CADisableMinimumFrameDurationOnPhone` as documented for Compose rendering ([SwiftUI integration](https://kotlinlang.org/docs/multiplatform/compose-swiftui-integration.html), [UIKit integration](https://kotlinlang.org/docs/multiplatform/compose-uikit-integration.html)).

Direct Xcode integration uses `embedAndSignAppleFrameworkForXcode` and is the default integration created by the KMP IDE tooling when the shared module does not need CocoaPods dependencies ([iOS integration methods](https://kotlinlang.org/docs/multiplatform/multiplatform-ios-integration-overview.html), [direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)).

### Recommended shape - architectural inference

```text
apps/mobile/
|-- androidApp/              Android application, manifest, signing, OS policy
|-- iosApp/                  Xcode project, Info.plist, signing, native host
`-- shared/
    `-- src/
        |-- commonMain/      UI, shell, features, navigation, state, domain,
        |                    repositories, DTOs/API, Room schema/DAOs
        |-- androidMain/     Android client engine, Context/paths, OS services
        `-- iosMain/         Darwin client engine, Apple paths, OS services
```

Keep the shared module internally modular by feature and layer, but begin with one physical KMP module unless build time or ownership provides evidence for splitting it. Multimodule KMP adds framework export and dependency-compatibility work without improving the one-person product by itself.

Platform entry points should construct a small `PlatformDependencies` object and invoke one shared `App(...)` composable. `expect`/`actual` should be reserved for irreducibly platform-specific primitives; prefer injecting narrow interfaces into common code so tests can replace them.

Compose Multiplatform resources can keep strings, fonts, and images in common source sets, with light/dark, locale, and density qualifiers ([resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html), [resource qualifiers](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html)). Because most packaged resources are read synchronously, large raw assets should not sit on critical composition paths.

## UI, navigation, back, and gesture ownership

### Supported options - sourced facts

Navigation 3 is supported by Compose Multiplatform on Android and iOS. It gives the application ownership of a snapshot-state back stack and low-level navigation building blocks. Non-JVM targets cannot use Android's reflection-based route serialization, so route keys require Kotlin serialization through `SavedStateConfiguration` ([Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)).

The earlier Navigation Compose 2.9.2 API is also a supported, stable option and includes type-safe routes, multiple back stacks, and iOS back-swipe handling. On iOS, its native-like back animation is enabled by default, but specifying custom `NavHost` enter/exit transitions replaces that default behavior ([Compose navigation](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)).

`HorizontalPager` exposes `userScrollEnabled`, stable page keys, a precomposition limit, fling behavior, and a nested-scroll connection. Disabling user scroll still permits programmatic page changes. Compose's nested-scroll protocol allows parents and children to pre-consume or post-consume deltas, but it does not decide the product's gesture priority ([HorizontalPager API](https://developer.android.com/reference/kotlin/androidx/compose/foundation/pager/HorizontalPager), [nested scrolling](https://developer.android.com/develop/ui/compose/touch-input/scroll/nested-scroll-modifiers)).

### Navigation choice - architectural inference

Adopt Navigation 3 for secondary destinations and dialogs, and model the top-level pane pager as explicit shell state rather than pretending each drag is an ordinary back-stack operation.

- Define serializable route keys in common code, grouped by feature behind sealed interfaces.
- Use Navigation 3 entry decorators for per-entry saveable state and ViewModel scope.
- Pass IDs, not domain objects or ViewModels, in routes; load current data from the repository.
- Treat the pager's selected pane and each pane's secondary destination stack as separate state.
- Use Navigation Event APIs for back progress; the older multiplatform `PredictiveBackHandler` is deprecated ([Compose 1.10 navigation changes](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html#deprecated-predictivebackhandler)).

Navigation 2 remains a viable fallback if a prototype exposes a Navigation 3 defect. No third-party navigation framework is needed by the current requirements.

### Gesture rule - architectural inference

The framework provides mechanisms, not automatic arbitration. Plannr must enforce one horizontal gesture owner at a time:

- The root pager owns horizontal drag only while a feature is in its overview/root mode.
- Opening a focused workspace, detail destination, editor, or edge-back transition disables root pager user scrolling.
- Programmatic pane changes and the visible navigation control continue to work while swipe paging is disabled.
- A vertical overscroll transition must consume only vertical intent after touch slop; it must not also leave the root horizontal pager active.
- Back first dismisses a dialog, then pops the focused destination/workspace, and only then exits the app. It should not page sideways.

The exact thresholds and motion remain prototype decisions. They must be tested on both platforms because KMP documentation explicitly notes that scrolling physics differ by platform and device ([KMP testing guidance](https://kotlinlang.org/docs/multiplatform/multiplatform-run-tests.html)).

## Lifecycle, presentation state, and restoration

### Sourced facts

Compose Multiplatform supplies a common `LifecycleOwner` and maps iOS view-controller/application notifications to AndroidX lifecycle states. It also supplies common ViewModel support. On non-JVM targets, ViewModels cannot be reflectively constructed, so common code must provide an initializer or factory ([lifecycle mapping](https://kotlinlang.org/docs/multiplatform/compose-lifecycle.html), [ViewModel construction](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html)).

Navigation 3 does not automatically scope ViewModels to entries. `rememberSaveableStateHolderNavEntryDecorator()` and `rememberViewModelStoreNavEntryDecorator()` must be installed to preserve entry Compose state and clear ViewModels at the right destination boundary ([ViewModel scoping](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html#viewmodel-scoping-with-navigation-3)).

Android's guidance distinguishes in-memory ViewModel state, small saveable UI state, and durable local storage. `rememberSaveable`/`SavedStateHandle` are for the minimum state needed to reconstruct UI, not large application data ([Android state-saving guidance](https://developer.android.com/topic/libraries/architecture/saving-states)).

Apple's scene/UI restoration is opt-in and requires the native host to preserve identifiers or a scene `NSUserActivity`; UIKit does not infer application-specific restoration data ([Apple state restoration](https://developer.apple.com/documentation/uikit/preserving-your-app-s-ui-across-launches), [scene restoration sample](https://developer.apple.com/documentation/uikit/restoring-your-app-s-state)).

### State model - architectural inference

- Each feature-level shared ViewModel exposes an immutable `StateFlow<UiState>` and accepts explicit intents.
- Repositories are the source of domain data; ViewModels retain presentation state and cancellation scopes, not duplicate caches.
- Save only compact navigation keys and ephemeral controls such as selected pane, selected entity ID, filter, or scroll anchor.
- Restore server-backed screen content by ID from Room, then refresh when connected.
- Do not persist transaction-editor drafts or queued mutations in the first release. Editing is enabled only while the server is reachable, matching the decided product boundary.
- Add explicit iOS scene-restoration plumbing for the small serializable shell/back-stack snapshot, and test a terminated relaunch. Common SavedState APIs do not remove that native-host responsibility.

## Networking and self-hosted-server constraints

### Sourced facts

Ktor's client API and plugins are multiplatform, while transport engines are platform-specific. Official Ktor documentation shows common client configuration with the `OkHttp` engine on Android and `Darwin` on iOS; Darwin uses `NSURLSession`. Engine capabilities and SSL configuration are not identical ([Ktor engines](https://ktor.io/docs/client-engines.html), [Ktor SSL](https://ktor.io/docs/client-ssl.html)).

Ktor Content Negotiation supports `kotlinx.serialization` JSON in shared code. Kotlin's JSON serialization format is stable on JVM, Android, and Native ([Ktor serialization](https://ktor.io/docs/client-serialization.html), [Kotlin serialization](https://kotlinlang.org/docs/serialization.html)).

Android disables cleartext HTTP by default for apps targeting API 28 or newer and supports narrowly scoped domain trust/cleartext exceptions through Network Security Configuration ([Android network security](https://developer.android.com/privacy-and-security/security-config)). Apple's App Transport Security requires HTTPS for `NSURLSession` and blocks connections that do not meet its security requirements unless the native app declares exceptions ([ATS](https://developer.apple.com/documentation/bundleresources/information-property-list/nsapptransportsecurity)).

iOS local-network access applies to direct TCP connections and requires a user-facing `NSLocalNetworkUsageDescription`. The first local operation may fail before the person answers the prompt, and the simulator does not reproduce local-network privacy behavior ([Apple local-network privacy](https://developer.apple.com/documentation/Technotes/tn3179-understanding-local-network-privacy), [`NSLocalNetworkUsageDescription`](https://developer.apple.com/documentation/bundleresources/information-property-list/nslocalnetworkusagedescription)).

### Client design - architectural inference

- Put DTOs, error mapping, request construction, timeout policy, JSON configuration, and the typed `PlannrApi` in common code.
- Construct one long-lived `HttpClient` per app process with shared plugins and injected engine configuration: OkHttp on Android, Darwin on iOS.
- Prefer HTTPS even for the personal server. If development needs HTTP or a private CA, configure the smallest host-specific native exception; never enable arbitrary cleartext or trust-all certificates globally.
- Treat initial iOS local-network denial/prompt and ATS failure as distinct, actionable connection states, not a generic server error.
- Make server URL validation shared, while keeping platform policy checks in adapters.
- Do not rely on transport-level automatic retries for mutations. A connection drop leaves the result unknown, and the first release has no idempotency/outbox contract.

## Persistence and offline-readable cache

### Sourced facts

Room supports KMP from 2.7.0. Current guidance recommends the bundled SQLite driver to avoid platform SQLite inconsistencies. Room KMP supports suspend DAOs and `Flow`; database construction and paths remain platform-specific. Query callbacks, auto-close, prepackaged databases, and multi-instance invalidation are among APIs not available in common code ([Room KMP setup and limits](https://developer.android.com/kotlin/multiplatform/room)).

DataStore supports KMP from 1.1.0, but only Preferences DataStore is supported for KMP. Its API can live in common code while Android and iOS provide the storage location ([DataStore KMP](https://developer.android.com/kotlin/multiplatform/datastore)).

### Storage choice - architectural inference

- Use Room with bundled SQLite for the normalized, last-successful server snapshot and cache metadata.
- Use Preferences DataStore for small installation settings: server URL, theme selection, and perhaps last selected top-level pane.
- Store cache timestamps per aggregate/query boundary so UI can explicitly mark stale data.
- On successful reads, update related Room rows atomically. On a confirmed successful mutation, update or invalidate the affected cache; on failure, retain the previous cache and show the failed edit as unsaved.
- Do not write speculative edits, drafts, a request outbox, or conflict metadata in the first release.
- Keep server DTOs, domain models, and Room entities separate. This allows server contracts and cache schema to migrate independently.

## Testing boundary

### Sourced facts

`commonTest` compiles for declared targets and uses `kotlin.test`; iOS simulator tests run with Kotlin/Native's test runner. Platform-specific tests remain available in their target source sets ([KMP tests](https://kotlinlang.org/docs/multiplatform/multiplatform-run-tests.html)).

Compose Multiplatform 1.11.1 provides v2 Compose UI test APIs on non-Android targets. Common Compose UI tests can run with `iosSimulatorArm64Test`, while Android requires configured device tests and `connectedAndroidTest`. The common Compose UI test API remains experimental ([Compose UI testing](https://kotlinlang.org/docs/multiplatform/compose-test.html), [1.11.1 test changes](https://kotlinlang.org/docs/multiplatform/whats-new-compose-111.html#compose-ui-tests-v2)).

Compose semantics map to Android accessibility/testing services and to native iOS accessibility properties. A Compose `testTag` maps to an iOS `accessibilityIdentifier`, so XCUITest can inspect the same shared UI ([iOS accessibility](https://kotlinlang.org/docs/multiplatform/compose-ios-accessibility.html)).

### Test strategy - architectural inference

1. Run common unit tests for validation, money/date calculations, reducers, route serialization, repository behavior, DTO compatibility, and cache policy.
2. Run Room migrations and repository integration tests on Android and `iosSimulatorArm64`.
3. Use shared Compose UI tests for deterministic component and feature-state behavior, while isolating the experimental test API behind test helpers.
4. Keep a small set of platform end-to-end tests:
   - Android instrumentation for process recreation, back, network-security configuration, and installation launch.
   - XCUITest on a real/simulated iPhone for back-edge gestures, keyboard/insets, scene restoration, local-network permission, and accessibility identifiers.
5. Exercise the Plannr API client against a controlled server fixture or contract test; do not make UI tests depend on the personal live server.

## Accessibility requirements

### Sourced facts

Compose semantics, traversal groups/order, content descriptions, roles, and state descriptions are available in shared UI. On iOS, Compose semantics are mapped to VoiceOver and XCTest. Material widgets cover many cases automatically, but custom controls still require explicit semantics ([Compose accessibility](https://kotlinlang.org/docs/multiplatform/compose-accessibility.html), [Compose semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics)).

Material 3's `ColorScheme` does not automatically provide an iOS high-contrast palette. Compose documentation recommends a separate palette selected from `UIAccessibilityDarkerSystemColorsEnabled`, and XCTest can run `performAccessibilityAudit()` against the mapped accessibility tree ([iOS accessibility and high contrast](https://kotlinlang.org/docs/multiplatform/compose-ios-accessibility.html)).

### Acceptance boundary - architectural inference

- Give every custom navigation, pager, financial summary, and editor control an explicit role, name, state, and logical traversal order.
- Never encode gains, losses, pending, stale, or error states by color alone.
- Define light, dark, and high-contrast semantic token sets in the shared design system; use a small platform adapter to observe system contrast and reduced-motion preferences.
- Test enlarged text, TalkBack, VoiceOver, switch/keyboard navigation, focus order, and dialogs on real devices.
- Provide non-gesture alternatives for paging and for entering/exiting the transaction workspace.
- Run Android accessibility checks and iOS `performAccessibilityAudit()`, but keep manual screen-reader review because automation cannot judge financial wording or gesture discoverability.

## Performance requirements

### Sourced facts

Compose performance depends on measured recomposition/stability, lazy composition, and release builds. Compiler reports can diagnose unstable parameters, but official guidance cautions against making everything skippable without evidence ([Compose stability](https://developer.android.com/develop/ui/compose/performance/stability), [stability diagnosis](https://developer.android.com/develop/ui/compose/performance/stability/diagnose)).

Android Baseline Profiles can cover startup, navigation, and scrolling; Macrobenchmark measures startup and frame/jank behavior in production-like builds ([Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/overview), [Compose Baseline Profiles](https://developer.android.com/develop/ui/compose/performance/baseline-profiles)).

Apple provides Time Profiler, Animation Hitches/Instruments, and XCTest metrics for launch, hitch, CPU, and memory measurement. Apple recommends profiling scrolling and animation on real, preferably older supported, devices ([Apple responsiveness](https://developer.apple.com/documentation/xcode/improving-app-responsiveness), [XCTest performance metrics](https://developer.apple.com/documentation/xctest/performance-tests)).

### Performance budget - architectural inference

- Compose only the visible pager page plus the framework's minimal prefetch; do not keep every future feature pane and every secondary workspace eagerly composed.
- Give pager pages and lazy-list items stable keys. Load long transaction histories with lazy lists and page data from the repository.
- Scope observable state to the smallest useful feature/subtree; do not make every pane collect one monolithic application state.
- Keep database, JSON transformation, sorting, and aggregation off the UI thread and precompute expensive chart/summary models.
- Benchmark the exact critical interactions: cold start to cached overview, pane swipe, vertical overview/workspace transition, opening pocket transactions, transaction list fling, and editor open/close.
- Measure release builds independently on Android and iPhone. A smooth Android trace is not evidence of iOS performance.
- Generate an Android Baseline Profile for those journeys and maintain an iOS XCTest/Instruments baseline. Treat regressions as test failures or release blockers once budgets are established by the prototypes.

## Constraints that later design tickets must retain

- Shared Compose UI is supported, but platform hosts and platform verification are permanent parts of the architecture.
- iOS 15, ARM64 device, and Apple-silicon simulator are the lowest-friction supported Apple baseline.
- Navigation and pager motion are separate concerns. Focused content must be able to disable root swipe paging.
- State restoration stores identifiers and navigation/presentation state, never a second copy of server data.
- The Room cache is read-only from the offline user's perspective. Mutations require a live server and are not queued.
- A configurable self-hosted URL entails Android network-security configuration, Apple ATS, and iOS local-network permission handling.
- The stable Compose release does not imply every satellite artifact is stable. Wrap Material 3 and experimental test APIs behind project-owned seams.
- Accessibility and performance parity are outcomes to measure per platform, not consequences of code sharing.

## Decision gist

Build Plannr as a shared KMP/Compose application library using the current stable Kotlin/Compose baseline, official Navigation/Lifecycle state, Ktor, Room, and DataStore; retain thin Android and iOS hosts for engines, paths, OS policy, restoration, accessibility, and profiling, and enforce exclusive gesture ownership whenever focused content is open.
