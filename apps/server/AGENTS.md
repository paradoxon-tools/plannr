# AGENTS.md

## Server rules

- Keep Bruno request files in sync with API and controller changes. When endpoints, paths, methods, request bodies, response expectations, or path/query parameters change, update the matching files under `.bruno`.
- Do not edit existing database migrations after they have been committed or applied. Add a new migration that builds on top of the existing sequence instead.
