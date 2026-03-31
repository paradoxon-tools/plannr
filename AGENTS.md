# AGENTS.md

## Project rules

- For domain data classes and value objects with private constructors, expose creation through `companion object { operator fun invoke(...) }` instead of named factory functions like `create(...)`.
- When applying that pattern, call sites should use the type name directly, e.g. `Currency(...)`, not `Currency.create(...)`.
- Do not rename controller handlers to `invoke` just for consistency with domain factories; use descriptive controller method names such as `create`, `update`, `getByCode`, or `list`.
