---
paths:
  - "packages/ui/src/components/**/*.tsx"
  - "apps/web/src/pages/**/*.tsx"
  - "apps/web/src/components/**/*.tsx"
---

# React Component Rules

## Page Components (apps/web/src/pages/)
- Pages are thin wrappers: import page component from `@pinpoint-fe/ui` and wrap with `withInitialFetch` HOC
- Always wrap with a layout component (`getLayoutWithSideNavigation` or `getLayoutWithConfiguration`)
- Pattern:
  ```tsx
  import { withInitialFetch, SomePageComponent } from '@pinpoint-fe/ui';
  import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';
  export default withInitialFetch((props) =>
    getLayoutWithSideNavigation(<SomePageComponent {...props} />),
  );
  ```

## Domain Components (packages/ui/src/components/)
- Place domain-specific components in their own subdirectory (e.g., `ServerMap/`, `ErrorAnalysis/`)
- Use Suspense boundaries and ErrorBoundary for async data
- Compose from UI primitives in `components/ui/`
- Accept typed props interfaces — define props above component

## UI Primitives (packages/ui/src/components/ui/)
- Follow shadcn/ui pattern: Radix UI primitives + CVA (class-variance-authority) + Tailwind
- Use `cn()` from `@pinpoint-fe/ui/src/lib/utils` for className merging (clsx + tailwind-merge)
- Support `asChild` prop via Radix Slot where applicable
- Use `React.forwardRef` for components needing ref forwarding
- Define `variants` via CVA for multi-variant components (button, badge, etc.)

## Styling
- Tailwind CSS classes only — no inline styles or CSS modules
- Custom colors use CSS variables: `--ui-primary`, `--ui-border`, etc.
- Status colors: `status-success`, `status-good`, `status-warn`, `status-fail`
- Speed colors: `fast`, `normal`, `delay`, `slow`, `error`
- Dark mode support via `class` strategy

## Lazy Loading
- All pages except ServerMap (the default route) use `React.lazy()`
- Lazy components defined in `apps/web/src/routes/index.tsx`
