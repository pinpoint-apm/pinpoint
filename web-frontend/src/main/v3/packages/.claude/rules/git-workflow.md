# Git Workflow Rules

## Branching
- **Never commit directly to master.** Always create a new branch with an appropriate name before committing.
- Branch naming: `fix/`, `feat/`, `refactor/`, etc. followed by a descriptive name (e.g., `fix/9520-preserve-timestamp-during-loading`).

## Pre-Commit Self Review (MANDATORY)
Before every commit, perform the following checks:
1. **Self review**: Re-read all changed code. Verify logic correctness, no debug code (console.log, etc.), and code style compliance.
2. **Regression check**: Ensure all existing functionality still works. Trace callers/consumers of modified code and confirm compatibility.
3. **Build**: Run `yarn build` and confirm it passes.
4. **Test**: Run `yarn test` and confirm all tests pass.

Do NOT commit if build or tests fail. Fix issues first.

## Commit Messages
- Format: `[#issue_number] Description` (e.g., `[#9520] Preserve timestamp during server map loading`)
- **Same platform**: If the issue and the repo are on the same platform (e.g., both on github.com, or both on oss.navercorp.com), use the issue number: `[#issue_number]`
- **Different platform**: If the issue and the repo are on different platforms (e.g., issue on oss.navercorp.com but repo on github.com), use `[#noissue]`
- Keep messages concise and focused on the "why" rather than the "what".

## Push
- **Always rebase onto upstream/master before pushing:**
  ```bash
  git fetch upstream master
  git rebase upstream/master
  git push -u origin <branch-name>
  ```
- Remote setup: `origin` = fork (jihea-park/pinpoint), `upstream` = pinpoint-apm/pinpoint.

## Pull Request Creation
- PRs target `upstream` (pinpoint-apm/pinpoint) master branch.
- **Always assign** the user (jihea-park) to the PR.
- **On GitHub repos**, request Copilot code review via REST API:
  ```bash
  gh api repos/{owner}/{repo}/pulls/{number}/requested_reviewers \
    --method POST -f 'reviewers[]=copilot'
  ```
  Note: `gh pr create --reviewer copilot` does not work for Copilot; use the REST API instead.
- **PR review feedback**: When the user provides review comments, evaluate each one. Apply code changes only when the feedback is valid and necessary. Do not modify code for comments that are unnecessary or incorrect — explain the reasoning instead.
- PR body format:
  ```
  ## Summary
  - <1-3 bullet points>

  ## Test plan
  - [ ] <checklist items>
  ```
