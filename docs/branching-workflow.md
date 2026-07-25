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

Open the pull request against `develop` and use **Rebase and merge**. Do not
create merge commits or squash commits. This keeps `develop` as one linear
sequence of features.

## Releases

At a chosen release point, ensure `develop` is current and open a pull request
from `develop` to `master`. GitHub requires that exact source branch and that
the pull request is up to date with `master`. Use **Rebase and merge**.

After it is merged, create the version tag and GitHub Release from the new
`master` commit. That tag is the immutable release point; no release branch is
needed or allowed to merge into `master`.

## Guardrails

- Direct pushes and force-pushes to `develop` and `master` are blocked.
- Pull requests are required for both protected branches.
- Only rebase-and-merge is enabled repository-wide.
- The required `master-source-branch` check fails every `master` pull request
  whose source is not `develop`.
