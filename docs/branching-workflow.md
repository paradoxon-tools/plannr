# Branching workflow

`develop` is the integration branch. It contains the ordered set of completed
features and is the only branch that may be promoted to `master`.

## Feature work

Create each feature from the latest `develop`. Before its pull request is
merged, rebase it onto the current `origin/develop` and update the remote
branch with `--force-with-lease`:

```sh
git fetch origin
git switch feature/my-feature
git rebase origin/develop
git push --force-with-lease
```

Open the pull request against `develop` and use **Rebase and merge** only after
the feature branch has been rebased. Do not create merge commits or squash
commits on `develop`. This keeps `develop` as one linear sequence of features:
each feature is replayed onto the current `develop` tip before `develop`
advances to include it.

## Releases

At a chosen release point, ensure `develop` is current and open a pull request
from `develop` to `master`. GitHub requires that exact source branch. Use
**Create a merge commit**. Never use **Rebase and merge** for a release pull
request: the merge commit keeps the existing `develop` commit IDs intact on
`master` and becomes the release commit.

After it is merged, GitHub creates the next minor version automatically. Release
tags use `v<major>.<minor>` (for example, `v1.4`): the first release is `v0.1`;
after that, the action finds the highest existing release tag for the current
major and increments its minor component. It publishes the GitHub Release from
the release PR's merge commit with generated notes, a directly linked mobile
APK, and a link to the versioned server image on GHCR. The action rejects a
release that does not have the reviewed `develop` tip as the second parent of a
two-parent merge commit.

Every major version is a deliberate, manual operation. After merging the
release PR, tag that exact `master` commit as `v<major>.0` (for example, `v1.0`
or `v2.0`) and push the tag. The manual-tag workflow publishes the release and
its artifacts. Subsequent `develop` to `master` promotions then increment that
major's minor version automatically.

No release branch is needed or allowed to merge into `master`.

## Guardrails

- Direct pushes and force-pushes to `develop` and `master` are blocked.
- Pull requests are required for both protected branches.
- Feature pull requests into `develop` require rebase merging.
- Release pull requests into `master` require a merge commit.
- The required `master-source-branch` check fails every `master` pull request
  whose source is not `develop`.
