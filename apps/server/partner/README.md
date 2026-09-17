# Partner logos

Partners expose an optional `logoVersion`. The corresponding image is served at
`GET /partners/{id}/logo/{logoVersion}` as PNG with immutable cache headers.

- `POST /partners/logo-preview` takes `{ "website": "https://example.com" }` and
  returns `{ "base64": "..." }`. It discovers HTML icon links (preferring Apple
  touch icons), falls back to common icon paths, or accepts a direct image URL.
  Nothing is persisted during preview, including the website/domain.
- `PUT /partners/{id}/logo` takes `{ "base64": "..." }` after the user accepts a
  preview or chooses an upload. It stores a normalized PNG and updates the hash.
- `DELETE /partners/{id}/logo` removes the saved logo.

Supported source images: PNG, JPEG, GIF and ICO; SVG is intentionally not accepted.
Images are limited to 1 MiB, 4096 pixels per side and 4 megapixels, then reduced to
at most 256 pixels per side. Metadata is discarded. Public HTTP(S) requests use
validated, pinned DNS addresses, no proxy, bounded bodies/timeouts and checked
redirects. Network failures are returned without echoing the input URL.

Deploy the server build with migration V43 before using logo saves in the mobile
app. Older server responses remain compatible: the app displays partner initials
until a logo exists. The mobile editor is under Wallet → Management → Partners
→ Select a partner; accepted logo changes apply immediately to that partner everywhere.
