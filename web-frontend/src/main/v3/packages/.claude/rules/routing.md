---
paths:
  - "apps/web/src/routes/**"
  - "packages/ui/src/loader/**"
  - "packages/ui/src/constants/path.ts"
---

# Routing Rules

## Structure
- React Router v6 with `createBrowserRouter`
- Routes defined in `apps/web/src/routes/index.tsx`
- Route paths defined in `packages/ui/src/constants/path.ts` (`APP_PATH`)
- `BASE_PATH` env var for subpath deployments

## Adding a New Route
1. Define path constant in `packages/ui/src/constants/path.ts` under `APP_PATH`
2. Create page component in `packages/ui/src/pages/`
3. Create page wrapper in `apps/web/src/pages/` using `withInitialFetch`
4. Create route loader in `packages/ui/src/loader/` if date validation needed
5. Add route to `apps/web/src/routes/index.tsx`
6. Use `React.lazy()` for code splitting (except default route)

## Route Loaders (`packages/ui/src/loader/`)
- Validate URL date parameters (from/to)
- Redirect with corrected date format if invalid
- Parse application from URL params
- Check against `periodMax` configuration for valid date ranges
- Use `SEARCH_PARAMETER_DATE_FORMAT` for canonical format

## URL Pattern
- Application routes: `/:pageName/:application?`
- Config routes: `/config/:configPage`
- Date params: `?from=YYYY-MM-DD-HH-mm-ss&to=YYYY-MM-DD-HH-mm-ss`
- Application format in URL: `applicationName@serviceType`
