# AGENTS.md

This file provides guidance to AI coding agents when working with code in this repository.

## ⛔ Non-Negotiable Rules (check before every task)

These rules must be followed **in every situation, for every file type, for any reason** — no exceptions.

### 1. Never commit directly to the master branch

Always check the current branch before committing:

```bash
git branch --show-current
```

If the result is `master`, **stop immediately** and create a branch first:

```bash
git fetch upstream master
git checkout -b <branch-name> upstream/master
```

> `.claude/` config files, README, one-line fixes — no exceptions. Never commit directly to master.

### 2. Always run `/qa-pr` before committing

Always run the `/qa-pr` skill before committing/pushing. If the QA verdict is FAIL, do not commit.

---

## Project Overview

Pinpoint Web Frontend v3 — the web UI for [Pinpoint APM](https://github.com/pinpoint-apm/pinpoint), an open-source application performance monitoring tool. It is a React 19 monorepo that provides server map visualization, scatter/heatmap charts, transaction tracing, error analysis, and configuration management.

## Commands

```bash
# Dev server (Vite dev server, port 3000, proxies /api/* to localhost:8080)
yarn dev

# Build (TypeScript check + Vite production build)
yarn build

# Lint (runs Prettier → ESLint on apps/web)
yarn lint

# Run tests across all workspaces
yarn test

# Run tests for a specific package
yarn workspace @pinpoint-fe/ui test

# Run a single test file
yarn workspace @pinpoint-fe/ui jest path/to/file.test.ts

# Storybook (UI component development, port 6006)
yarn workspace @pinpoint-fe/ui storybook

# E2E tests (Playwright)
yarn workspace @pinpoint-fe/web test:e2e

# Clean everything
yarn clean
```

Requires Node >=22.13.1 and Yarn 1.22.22.

## Monorepo Structure

```
apps/web/           → @pinpoint-fe/web     Main app (thin page wrappers + routing)
packages/ui/        → @pinpoint-fe/ui      Core library: components, hooks, atoms, constants, loaders
packages/scatter-chart/ → @pinpoint-fe/scatter-chart  Canvas-based scatter chart (no React dependency in core)
packages/server-map/    → @pinpoint-fe/server-map     Cytoscape-based network topology graph
packages/datetime-picker/ → @pinpoint-fe/datetime-picker  Date/time picker component
```

Packages are linked via Yarn workspaces. Internal dependencies use the `"*"` version.

## Architecture

### Page → Component → Hook Pattern

Pages in `apps/web/src/pages/` are thin wrappers. Almost all logic lives in `packages/ui/`:

```
apps/web/src/pages/Inspector.tsx  →  wraps InspectorPage from @pinpoint-fe/ui
                                     and reads configuration from configurationAtom
```

**Nested layout routes** (`apps/web/src/components/Layout/`): routing uses React Router's nested `<Outlet />` components to handle cross-cutting concerns:

- `SideNavigationOutlet` — wraps pages with the side navigation layout
- `InitialFetchOutlet` — fetches configuration, syncs URL search params into Jotai atoms, redirects to `/apiCheck` on error
- `ConfigurationOutlet` — wraps configuration pages with the configuration layout

**Route loaders** (`packages/ui/src/loader/`): React Router loaders validate/normalize URL date params (from/to) before rendering. Invalid date formats are redirected to the correct format.

### State Management

- **Jotai** atoms (`packages/ui/src/atoms/`) — global state for search params, server map data, scatter data, configuration, transactions, toasts, etc.
- **React Query** (`@tanstack/react-query`) — all server data fetching. Custom hooks in `packages/ui/src/hooks/api/` follow the `useGetXxx` pattern: they wrap `useQuery` with an endpoint from `END_POINTS` and the shared `queryFn`
- URL search params (`from`, `to`, application) are the source of truth for the time range and the selected application, and are synced into atoms via `InitialFetchOutlet`

### API Layer

- Endpoints are defined in `packages/ui/src/constants/EndPoints.ts` — all prefixed with `/api`
- The shared `queryFn` lives in `packages/ui/src/hooks/api/reactQueryHelper.ts`; it uses native `fetch` and parses ProblemDetail-style error responses
- The dev server proxies `/api/*` to `localhost:8080` (Pinpoint backend)
- Route paths are defined in `APP_PATH` in `packages/ui/src/constants/path.ts`

### UI Components

- `packages/ui/src/components/ui/` — Radix UI primitives styled with Tailwind (shadcn/ui pattern: accordion, dialog, dropdown, tooltip, etc.)
- `packages/ui/src/components/` — domain components (ServerMap, Chart, Transaction, Inspector, etc.)
- Styling: Tailwind CSS with `class-variance-authority` + `clsx` + `tailwind-merge`

### Visualization Libraries

- **scatter-chart**: standalone canvas-based scatter plot, no React in core
- **server-map**: Cytoscape + dagre layout to display the application dependency topology
- Both libraries are used through wrapper components in `packages/ui/src/components/`

## Code Style

- Prettier: 100-character width, single quotes, trailing commas, semicolons
- ESLint: flat config (`eslint.config.js`), TypeScript-ESLint + Prettier integration
- TypeScript strict mode, ESNext target, `react-jsx` transform
- Imports from `@pinpoint-fe/ui` use deep paths: `@pinpoint-fe/ui/src/constants`, `@pinpoint-fe/ui/src/hooks`, etc.
- All pages except ServerMap (the default route) are lazy-loaded with `React.lazy()`
- i18n: `i18next` with English and Korean locales in `packages/ui/src/constants/locales/`

## Finding `.claude/` Resources (Important)

`.claude/` configuration files (skills, rules, commands, settings, etc.) are spread across several directories in this monorepo. **When looking for `.claude/` resources**, if they are not found in the current working directory, search upward through parent directories:

1. Check `<cwd>/.claude/` first
2. If not found, search parent directories: the root `.claude/`
3. Known locations:
   - `.claude/skills/` — add-translation, apply-review, create-api-hook, create-component, create-page, review-code, qa-pr, merge-check, write-test, mvn-web
   - `.claude/agents/` — code-reviewer, explorer, debugger, qa-engineer
   - `.claude/rules/` — code-style, monorepo, git-workflow, tool-usage, code-review-policy, service-map

This rule applies to **all** `.claude/` resources (skills, rules, commands, and other settings).

## Build Integration

The web app output is deployed as static assets inside the Java/Maven backend. `yarn move:dist` copies `dist/` to `target/classes/static/`. The `BASE_PATH` environment variable controls the serving sub-path.
