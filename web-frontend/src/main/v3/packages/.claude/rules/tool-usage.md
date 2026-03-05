# Tool Usage Rules

## GitHub/Issue Access Priority

When the user provides a link from `oss.navercorp.com` or `github.com/pinpoint-apm/pinpoint`:

1. **Always use `gh` CLI first**, before attempting MCP GitHub tools.
2. Examples:
   ```bash
   # View an issue
   gh issue view <number> --repo <owner/repo>

   # View a PR
   gh pr view <number> --repo <owner/repo>

   # List issue comments
   gh issue view <number> --repo <owner/repo> --comments
   ```
3. This applies to issues, PRs, and any other GitHub-accessible resources.
4. Only fall back to MCP tools if `gh` CLI is unavailable or insufficient for the task.
