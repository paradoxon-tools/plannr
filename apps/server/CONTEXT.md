# Plannr Server

Plannr Server owns the financial planning model, recurring transaction schedules, and their projections.

## Language

**Transaction template**:
A recurring transaction's stable identity and ownership details, shared by every effective-dated version of its amount and recurrence schedule.
_Avoid_: Recurring transaction

**Transaction template version**:
One effective-dated segment of a transaction template's amount and recurrence schedule.
_Avoid_: Template update, price override

**Correction**:
An in-place rectification of one transaction template version when its stored details were erroneous; correcting a successor's start also corrects its predecessor's end boundary.
_Avoid_: Version, fee change

**Bank connection**:
A consent attempt and resulting Enable Banking session for a personal bank. Disconnecting revokes access without deleting local history.

**Bank account**:
A durable identity discovered through bank consent. Provider identification hashes preserve identity across session renewal. An explicit link maps it to one manually configured planning account.

**Actual transaction**:
A locally stored bank observation with its original booking status, exact amount and provider evidence. It is independent of the transaction template and its materializations.

**Reconciliation**:
A confirmed one-to-one match between a booked actual transaction and the account leg of a planned occurrence. It retains a planned snapshot and survives materialization rebuilds. Transfers may be reconciled independently on each account.
