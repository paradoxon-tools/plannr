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
