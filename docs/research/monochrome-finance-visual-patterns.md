# Monochrome finance-app visual patterns

Research for [Study monochrome finance-app visual patterns](https://github.com/paradoxon-tools/plannr/issues/12), conducted 2026-07-31.

## Decision

Plannr should use a **role-based reversible neutral system**, not a literal per-pixel black/white inversion and not a Trade Republic replica:

- Light and dark share the same semantic tokens and hierarchy. The canvas, low-emphasis surface, raised surface, strong text, secondary text, divider, and disabled roles each receive independently tested light and dark values.
- Black/white carry hierarchy and primary interaction. Color has a strict budget: one optional interaction accent, plus semantic colors used only when a state genuinely has meaning.
- Money direction is expressed by sign, label, icon, and position. Green/red are reserved for success/favorable and error/unfavorable states; income is not automatically “good” and spending is not automatically “bad.”
- A feature or entity color may identify a pocket, account, or chart series, but remains a small marker or data encoding rather than a large decorative fill.
- Screens are single-column and data-first: one dominant contextual figure, compact summaries, clearly headed sections, scan-friendly rows, and progressive disclosure into details or focused workspaces.
- Typography, spacing, dividers, and tonal surfaces establish hierarchy. Gradients, heavy shadows, ornamental cards, and promotional modules do not.
- Motion explains continuity or acknowledges an action. It is brief, interruptible, tied to the gesture where applicable, and replaceable by a reduced-motion transition.

This is a visual-system direction for prototypes. It does **not** select final color values, typefaces, component geometry, or motion timings.

## Evidence and interpretation

### Trade Republic: useful restraint, not a template

Trade Republic currently frames its product around three short verbs—invest, spend, and bank—and its current official store media uses very large black type on an off-white field around small, focused captures of the app. Inside those captures, hierarchy comes mainly from type scale and weight, neutral space, thin separation, and right-aligned amounts. Color is scarce and local: for example, green marks an active state and small colored symbols distinguish transaction rows. **Visual inference:** this makes the financial data feel primary and the chrome secondary. It does not establish exact colors, measurements, fonts, or component behavior for Plannr. [Trade Republic on the App Store](https://apps.apple.com/de/app/trade-republic-broker-bank/id1410703839?l=en-GB), [Trade Republic on Google Play](https://play.google.com/store/apps/details?hl=en_US&id=de.traderepublic.app)

The product is broader than investing, but its own help paths still use a small number of short, stable places: `Wealth`, `Cash`, and an avatar for profile/settings. Transactions are reached within their owning context; for example, card transactions are below `Cash`, while savings-plan orders live under `Wealth > Orders & Insights`. This is first-party evidence for keeping contextual details inside a feature rather than turning each financial object into global navigation. [Trade Republic card-transaction help](https://support.traderepublic.com/en-de/1630-Can-I-use-my-card-abroad), [Trade Republic savings-plan help](https://support.traderepublic.com/en-de/842)

The official store media is promotional material, not an end-to-end usability recording. Its generous whitespace is therefore a tone reference, **not** a density specification. Static media also provides no reliable evidence about production motion or accessibility behavior.

### N26: tonal grouping and a whole-finances overview

N26's current official store media shows a whole-finances screen with a headline balance, accounts/spaces, quick actions, and a small chart in one vertical composition. It groups information with pale tonal surfaces and restrained borders rather than elevating every block. The same listing explicitly advertises system-aware dark mode, and N26's first-party dark-mode announcement describes light/dark as two appearances of the same app rather than different information architectures. **Visual inference:** reversible themes work best when information roles remain fixed and only their tonal expression changes. [N26 on Google Play](https://play.google.com/store/apps/details?hl=en-US&id=de.number26.android), [N26 dark-mode announcement](https://n26.com/en-fr/blog/dark-mode-is-now-available-to-all-n26-customers)

N26's first-party product material treats Spaces as subaccounts and Insights as a way to understand spending, while the current app listing combines banking, saving, investing, and dark mode in one product. This supports a shallow top-level structure with contextual summaries and detail flows. It does not imply that Plannr should copy N26's card art, teal brand color, bottom navigation, or screen arrangement. [N26 banking features](https://n26.com/en-eu/banking-features), [N26 App Store listing](https://apps.apple.com/de/app/n26-love-your-bank/id956857223?l=en-GB)

### Revolut: dark surfaces and bounded data color

Revolut's current official store media provides a useful dark-theme counterpoint: near-black canvas, charcoal containers, strong white numerical hierarchy, thin outlines, and bright color concentrated in a chart, category marker, or focal action. **Visual inference:** a black theme still needs multiple neutral surface levels; literal black everywhere would erase grouping. Color can remain vivid when its area is tightly bounded and its role is explicit. [Revolut on Google Play](https://play.google.com/store/apps/details?id=com.revolut.revolut), [Revolut on the App Store](https://apps.apple.com/de/app/revolut-banking-trading/id932493382)

Revolut's first-party help puts spending and income analytics behind `Home` and a contextual analytics control. The analytics then changes timeframe and groups the same data by category, merchant, country, currency, or card. This is evidence for keeping a stable financial home while letting focused views reshape the data, rather than promoting every view to global navigation. [Revolut spending and income analytics](https://help.revolut.com/en-FR/help/accounts/budget-and-analytics/how-can-i-see-my-spending-and-income-analytics/)

## Plannr visual-system requirements

### Neutral roles

Define roles before values. At minimum:

| Role | Light intent | Dark intent |
| --- | --- | --- |
| Canvas | off-white or white base | near-black base |
| Surface | subtle gray grouping | charcoal grouping |
| Raised/selected surface | strongest light neutral | stronger dark neutral |
| Text / strong | near-black | near-white |
| Text / secondary | quieter neutral with compliant contrast | quieter neutral with compliant contrast |
| Divider / outline | visible but subordinate | visible but subordinate |
| Disabled | visibly unavailable, never confused with secondary content | same semantic state |
| Scrim | darkens content without changing its hierarchy | same purpose, separately tuned |

“Reversible” means each role has an equivalent purpose in both themes. It does not mean computing one theme by negating RGB values. Final tokens must be tested as adjacent pairs in both appearances and in increased-contrast settings.

Use no more than three persistent surface levels in the main Finances pane. If every section becomes a rounded card, the page turns into a dashboard of equally important objects and loses scan order.

### Accent and semantic color

Adopt these rules:

1. Primary actions work in monochrome first: inverse filled button, outline button, or high-contrast text action.
2. An interaction accent is optional and singular. Prototype a restrained cool hue, but choose it only after testing both themes, color-vision simulations, and entity colors.
3. Semantic success, error, warning/pending, and informational/stale roles are separate tokens with light/dark variants.
4. Never encode state with hue alone. Pair it with text, sign, icon, shape, pattern, or line style.
5. Amount direction remains typographically neutral by default. A negative balance or failed payment may be semantically adverse; an ordinary expense is merely an outgoing transaction.
6. Pocket/account colors are identity metadata. Keep labels readable without them and provide a non-color identifier for charts and legends.

This follows Apple's guidance to use color consistently, make it work in light, dark, and increased-contrast contexts, and convey information with more than color alone. [Apple color guidance](https://developer.apple.com/design/human-interface-guidelines/color), [Apple accessibility guidance](https://developer.apple.com/design/human-interface-guidelines/accessibility)

### Information hierarchy and typography

- Show one dominant number for the current context: overall position on Finances, pocket balance on a pocket, transaction amount in transaction details.
- Put the period, data freshness, and scope adjacent to that number. A large number without scope is visually strong but financially ambiguous.
- Use section headings and whitespace for major groups; use dividers or small tonal changes inside a group.
- Align comparable amounts on the trailing edge. Keep labels on the leading edge and make the entire row the target.
- Use a legible sans-serif family with clear tabular numerals, distinct signs, and unambiguous `0/O` and `1/l`. Typeface selection remains a prototype decision.
- Use weight and size sparingly. Avoid an ultra-light secondary style: in a monochrome system, low-contrast text quickly becomes unreadable.
- Allow labels and money values to wrap or reflow under large text. Do not shrink financial values until they fit.
- Charts need a textual summary, labelled values, a declared period, and a non-color way to distinguish series. Decorative sparklines must not carry unique information.

### Density

Minimal does not mean empty. Plannr needs to scan more entities than the store advertisements show.

- Keep the main pane vertically efficient by avoiding nested card padding and repeated hero blocks.
- Prefer one compact row per entity/transaction with a secondary line only when it adds decision-relevant information.
- Preserve at least a 48 dp interactive area in the shared Compose UI, even when the visible icon or divider is smaller. Compose Material components provide this minimum only when used with their intended interactive APIs; custom rows and icon buttons still need verification. [Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- Test at Android's 200% font scale and iOS large accessibility text sizes. Android explicitly requires layouts to accommodate its nonlinear 200% scaling rather than assuming proportional dimensions. [Android nonlinear font scaling](https://developer.android.com/about/versions/14/features#accessibility)
- Let large text increase page height. The main pane is already scrollable; preserving information is more important than preserving a screenshot-like fold.

### Navigation and contextual views

- Use short feature names in the global pager (`Finances`, not a list of finance subfeatures).
- Keep settings/profile outside the feature pager.
- Treat account, pocket, goal, and contract details as contextual scopes. Their transaction lists and analytics belong to that scope.
- Keep one stable visual route back to the owning scope. A gesture may accelerate the transition, but cannot be the only route.
- Do not add global destinations merely because a data set has several filters or visualizations.

These are visual-architecture constraints, not a decision about horizontal/vertical gesture ownership; that must be settled by the shell and transaction-workspace prototypes.

### Motion

Static store media cannot establish how the comparator apps actually animate. Plannr should therefore follow platform behavior and validate its distinctive transitions directly:

- Animate continuity, spatial relationship, loading, or confirmed state change—not decoration.
- Keep gesture-driven motion directly coupled to the finger and make cancellation predictable.
- Do not animate every number during refresh; use a stable layout and targeted state feedback.
- Under reduced motion, replace large axis travel, scaling, blur, and bounce with short fades or immediate state changes.
- Preserve the destination and focus semantics when motion is removed.

Apple specifically recommends responding to Reduce Motion by reducing repetitive, zooming, scaling, depth, and peripheral movement, and suggests fades in place of axis transitions. [Apple accessibility guidance](https://developer.apple.com/design/human-interface-guidelines/accessibility), [Apple motion guidance](https://developer.apple.com/design/human-interface-guidelines/motion)

## Accessibility acceptance criteria for prototypes

Treat these as gates, not later polish:

- Normal text reaches at least 4.5:1 contrast; large text and essential non-text UI reach at least 3:1. Test every relevant adjacent token pair in light and dark. These are WCAG 2.2 AA baselines and match Apple's published contrast guidance. [WCAG 2.2 contrast](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum), [Apple accessibility guidance](https://developer.apple.com/design/human-interface-guidelines/accessibility)
- Every actionable element has a 48 dp shared touch target and a meaningful accessibility role, state, and label.
- TalkBack and VoiceOver traversal follows the visible hierarchy; decorative visuals are excluded and charts expose an equivalent summary.
- Dynamic text does not clip, overlap, hide controls, or force horizontal scrolling at the tested platform maxima.
- Selection, gains/losses, pending/error, and chart series remain distinguishable in grayscale and common color-vision simulations.
- Reduced-motion mode preserves every workflow, including transitions into transaction workspaces.
- Any swipe, pull, long-press, or overscroll action has a visible tap alternative.
- Android prototypes run Compose's automated accessibility checks for labels, contrast, target size, and traversal order, followed by manual TalkBack testing. [Compose accessibility testing](https://developer.android.com/develop/ui/compose/accessibility/testing)

## Prototype questions this research leaves open

1. Which neutral values preserve a visibly black/white identity while keeping comfortable contrast over long sessions?
2. Does Plannr need an interaction accent at all, or can semantic and entity color be the only chromatic elements?
3. Which typography and numeric styles stay compact at normal size and reflow cleanly at accessibility sizes on Android and iOS?
4. What is the minimum useful summary above the fold before density becomes either promotionally sparse or dashboard-like?
5. How do selected pager state, selected financial scope, and semantic status remain visually distinct without competing accent systems?
6. Which transaction-workspace transitions retain spatial clarity when Reduce Motion is enabled?

## What must not be copied

Do not reproduce comparator brand colors, typefaces, logos, card art, icons, promotional imagery, screen composition, exact component shapes, copy, or animation choreography. The reusable material here is the role structure: restrained neutrals, bounded semantic color, clear numerical hierarchy, contextual drill-down, and equivalent light/dark information architecture.
