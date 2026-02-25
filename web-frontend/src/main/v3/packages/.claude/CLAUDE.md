# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pinpoint Web Frontend v3 — the web UI for [Pinpoint APM](https://github.com/pinpoint-apm/pinpoint), an open-source application performance monitoring tool. This is a React 19 monorepo that provides server map visualization, scatter/heatmap charts, transaction tracing, error analysis, and configuration management.

## Commands

```bash
# Development (Vite dev server on port 3000, proxies /api/* to localhost:8080)
yarn dev

# Build (TypeScript check + Vite production build)
yarn build

# Lint (runs Prettier then ESLint on apps/web)
yarn lint

# Run all tests across workspaces
yarn test

# Run tests in a specific package
yarn workspace @pinpoint-fe/ui test

# Run a single test file
yarn workspace @pinpoint-fe/ui jest path/to/file.test.ts

# Storybook (UI component development on port 6006)
yarn workspace @pinpoint-fe/ui storybook

# E2E tests (Playwright)
yarn workspace @pinpoint-fe/web test:e2e

# Clean everything
yarn clean
```

Requires Node >=22.13.1 and Yarn 1.22.22.

## Monorepo Structure

```
apps/web/           → @pinpoint-fe/web     Main application (thin page wrappers + routing)
packages/ui/        → @pinpoint-fe/ui      Core library: components, hooks, atoms, constants, loaders
packages/scatter-chart/ → @pinpoint-fe/scatter-chart  Canvas-based scatter chart (no React dependency in core)
packages/server-map/    → @pinpoint-fe/server-map     Cytoscape-based network topology graph
packages/datetime-picker/ → @pinpoint-fe/datetime-picker  Date/time picker component
```

Yarn workspaces link packages. Internal deps use `"*"` version.

## Architecture

### Page → Component → Hook pattern

Pages in `apps/web/src/pages/` are thin wrappers. Almost all logic lives in `packages/ui/`:

```
apps/web/src/pages/Inspector.tsx  →  wraps InspectorPage from @pinpoint-fe/ui
                                     reads configuration from configurationAtom
```

**Nested Layout Routes** (`apps/web/src/components/Layout/`): The routing uses React Router nested `<Outlet />` components to handle cross-cutting concerns:
- `SideNavigationOutlet` — wraps pages with the side navigation layout
- `InitialFetchOutlet` — fetches configuration, syncs URL search params to Jotai atoms, and redirects to `/apiCheck` on error
- `ConfigurationOutlet` — wraps configuration pages with the configuration layout

**Route loaders** (`packages/ui/src/loader/`): React Router loaders validate/normalize URL date params (from/to) before rendering. They redirect with corrected date formats when needed.

### State Management

- **Jotai** atoms in `packages/ui/src/atoms/` — global state for search parameters, server map data, scatter data, configuration, transactions, toasts, etc.
- **React Query** (`@tanstack/react-query`) — all server data fetching. Custom hooks in `packages/ui/src/hooks/api/` follow pattern: `useGetXxx` wraps `useQuery` with endpoint from `END_POINTS` and shared `queryFn`.
- URL search params (`from`, `to`, application) are the source of truth for time ranges and selected application, synced to atoms via `InitialFetchOutlet`.

### API Layer

- Endpoints defined in `packages/ui/src/constants/EndPoints.ts` — all prefixed with `/api`
- Shared `queryFn` in `packages/ui/src/hooks/api/reactQueryHelper.ts` uses native `fetch`, parses ProblemDetail-style error responses
- Dev server proxies `/api/*` to `localhost:8080` (the Pinpoint backend)
- Route paths defined in `packages/ui/src/constants/path.ts` (`APP_PATH`)

### UI Components

- `packages/ui/src/components/ui/` — Radix UI primitives styled with Tailwind (shadcn/ui pattern: accordion, dialog, dropdown, tooltip, etc.)
- `packages/ui/src/components/` — domain components (ServerMap, Chart, Transaction, Inspector, etc.)
- Styling: Tailwind CSS with `class-variance-authority` + `clsx` + `tailwind-merge`

### Visualization Libraries

- **scatter-chart**: Standalone canvas-based scatter plot, no React in core
- **server-map**: Cytoscape + dagre layout for application dependency topology
- Both are consumed by wrapper components in `packages/ui/src/components/`

## Code Style

- Prettier: 100 char width, single quotes, trailing commas, semicolons
- ESLint: flat config (`eslint.config.js`), TypeScript-ESLint + Prettier integration
- TypeScript strict mode, ESNext target, `react-jsx` transform
- Imports from `@pinpoint-fe/ui` use deep paths: `@pinpoint-fe/ui/src/constants`, `@pinpoint-fe/ui/src/hooks`, etc.
- Lazy loading via `React.lazy()` for all pages except ServerMap (the default route)
- i18n: `i18next` with English and Korean locales in `packages/ui/src/constants/locales/`

## Build Integration

The web app output is deployed as static assets within a Java/Maven backend. `yarn move:dist` copies `dist/` to `target/classes/static/`. `BASE_PATH` env var controls the serving subpath.
