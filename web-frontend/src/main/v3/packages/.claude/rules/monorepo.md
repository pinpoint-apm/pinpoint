# Monorepo Rules

## Package Structure
- `apps/web/` (`@pinpoint-fe/web`): Main application — thin page wrappers + routing only
- `packages/ui/` (`@pinpoint-fe/ui`): Core library — all components, hooks, atoms, constants
- `packages/scatter-chart/` (`@pinpoint-fe/scatter-chart`): Canvas-based scatter chart
- `packages/server-map/` (`@pinpoint-fe/server-map`): Cytoscape topology graph
- `packages/datetime-picker/` (`@pinpoint-fe/datetime-picker`): Date/time picker

## Dependency Rules
- `apps/web` depends on `@pinpoint-fe/ui` — import via deep paths
- `@pinpoint-fe/ui` depends on scatter-chart, server-map, datetime-picker
- Internal deps use `"*"` version in package.json
- Yarn workspaces link packages for local dev

## Where to Put Code
- **New component**: `packages/ui/src/components/`
- **New hook**: `packages/ui/src/hooks/`
- **New constant/type**: `packages/ui/src/constants/`
- **New atom**: `packages/ui/src/atoms/`
- **New utility**: `packages/ui/src/utils/`
- **New page**: Page component in `packages/ui/src/pages/`, wrapper in `apps/web/src/pages/`
- **New route**: `apps/web/src/routes/index.tsx`

## Commands
- Always use `yarn workspace <package-name>` for package-specific commands
- Root `yarn dev`, `yarn build`, `yarn lint`, `yarn test` delegate to the appropriate workspace
