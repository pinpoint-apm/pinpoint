---
name: create-page
description: Create a new page following the Pinpoint project pattern. Use when adding a new route/page to the application.
argument-hint: "[page-name] [description]"
allowed-tools: Read, Glob, Grep, Edit, Write
---

# Create a New Page

Create a new page named `$0` with the following description: `$ARGUMENTS`.

Follow these steps strictly:

## 1. Analyze Existing Pages
- Read `packages/ui/src/constants/path.ts` to understand APP_PATH pattern
- Read `apps/web/src/routes/index.tsx` to see routing structure
- Read an existing similar page in `packages/ui/src/pages/` and its wrapper in `apps/web/src/pages/`

## 2. Create Files (in order)

### 2a. Add Route Path
Add the new path constant to `APP_PATH` in `packages/ui/src/constants/path.ts`.

### 2b. Create Page Component
Create `packages/ui/src/pages/{PageName}.tsx`:
- Import hooks and components from `@pinpoint-fe/ui`
- Accept `configuration` prop from `withInitialFetch`
- Use appropriate search parameter hook
- Use React Query hooks for data fetching

### 2c. Export Page Component
Export the page from `packages/ui/src/pages/index.ts` (or create if needed).

### 2d. Create Route Loader (if date params needed)
Create `packages/ui/src/loader/{pageName}.ts` following the pattern in existing loaders:
- Parse application from URL params
- Validate date range against `periodMax` configuration
- Redirect with corrected dates if invalid

### 2e. Create Page Wrapper
Create `apps/web/src/pages/<PageName>.tsx`:
```tsx
// Replace <PageName> with the actual page name (e.g., Inspector, ErrorAnalysis)
import { withInitialFetch, <PageName>Page } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(<<PageName>Page {...props} />),
);
```

### 2f. Add Route
Add the route to `apps/web/src/routes/index.tsx`:
- Add lazy import at the top
- Add route entry with loader and element

## 3. Add i18n Keys
Add any user-facing strings to both `en.json` and `ko.json` in `packages/ui/src/constants/locales/`.

## 4. Verify
- Check that the route path is unique
- Confirm all imports are correct
- Verify the page follows withInitialFetch pattern
