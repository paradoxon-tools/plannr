# Plannr Mobile

The shared client experience through which Android and iOS users interact with Plannr's present and future feature areas.

## Language

**Feature pane**:
A top-level application destination for one feature area. It consolidates that area's primary information into one vertically scrollable surface while dialogs and secondary screens hold focused workflows.
_Avoid_: Feature tab, navigation page

**Dashboard**:
The cross-feature pane that prioritizes what deserves the user's attention across Plannr. It may reuse summaries from other feature areas, but does not replace their inspection and management surfaces.
_Avoid_: Home, Finances overview

**Finances**:
The feature pane for inspecting a user's financial position and its composition, judging whether goals and obligations are adequately funded, and working with accounts, pockets, contracts, saving goals, historical transactions, and upcoming transactions. It may use upcoming activity to explain financial state, but cross-feature attention prioritization belongs to Dashboard. Its short name is also its navigation label.
_Avoid_: Money Management, finance dashboard

**Account balance**:
The sum of the current balances of every pocket belonging to an account. It represents the total funds held in that account.
_Avoid_: Total balance, combined pocket balance

**Available pocket**:
The default pocket of an account, holding funds not allocated to another pocket. It is presented to the user as **Available** regardless of its stored name.
_Avoid_: Default pocket, available cash

**Available balance**:
The current balance of an account's available pocket, representing funds not allocated to another pocket.
_Avoid_: Free balance, spendable balance

**Funding health**:
The time-based relationship between the funds currently allocated to a contract or saving goal and its funding schedule, expressed as a duration ahead, on track, or behind. Contract and obligation funding health is kept separate from saving-goal funding health when summarized.
_Avoid_: Completion percentage, financial position

**Financial profile**:
A named classification connecting contracts, saving goals, and transaction entries that belong to the same financial context.
_Avoid_: Account, category, tag

**Default financial profile**:
The financial profile automatically selected when a new relationship is created without an explicit profile choice. Any assignable financial profile may be the default.
_Avoid_: Unassigned profile, fallback profile

**Unassigned profile**:
The protected fallback financial profile used when a relationship has no meaningful profile. Its identity is represented by the absence of a profile indicator rather than by displaying an Unassigned avatar.
_Avoid_: Default financial profile

**Upcoming summary**:
The signed net change and transaction-entry count expected within a user-selected future window. It combines all accounts on the Finances overview and adopts the current account, pocket, contract, or saving-goal scope on a focused screen.
_Avoid_: Forecast balance, future transaction list

**Transaction entry**:
A one-off or recurring income, expense, or transfer defined through a transaction template.
_Avoid_: Payment

**Transaction feed**:
A chronological, balance-aware view of transaction history scoped to one account, pocket, or contract.
_Avoid_: Transaction view, transaction list

**Server connection**:
The configured endpoint for the single private, self-hosted Plannr Server installation used by the app's sole user.
_Avoid_: Account, tenant
