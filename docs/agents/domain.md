# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- **`CONTEXT-MAP.md`** at the repo root — it points to the domain documentation for the server and each client application. Read every context relevant to the topic.
- **`docs/adr/`** — read system-wide ADRs that touch the area you're about to work in.
- **`apps/<application>/docs/adr/`** — read ADRs scoped to each relevant server or client context.

If any of these files don't exist, **proceed silently**. Don't flag their absence or suggest creating them upfront. The `/domain-modeling` skill, reached via `/grill-with-docs` and `/improve-codebase-architecture`, creates them lazily when terms or decisions are resolved.

## File structure

```text
/
├── CONTEXT-MAP.md
├── docs/
│   └── adr/                       ← system-wide decisions
└── apps/
    ├── server/
    │   ├── CONTEXT.md
    │   └── docs/
    │       └── adr/               ← server-specific decisions
    └── mobile/
        ├── CONTEXT.md
        └── docs/
            └── adr/               ← mobile-client decisions
```

Add future client applications under `apps/`, each with its own `CONTEXT.md` and context-specific `docs/adr/` directory, and link them from the root `CONTEXT-MAP.md`.

## Use the glossary's vocabulary

When your output names a domain concept—in an issue title, refactor proposal, hypothesis, or test name—use the term as defined in the relevant `CONTEXT.md`. Don't drift to synonyms the glossary explicitly avoids.

If the concept you need isn't in the glossary yet, that's a signal: either you're inventing language the project doesn't use and should reconsider, or there's a real gap to note for `/domain-modeling`.

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly instead of silently overriding it:

> _Contradicts ADR-0007 (event-sourced orders)—but worth reopening because…_
