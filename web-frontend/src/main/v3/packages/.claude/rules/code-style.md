# Code Style Rules

## Formatting (Prettier)
- Line width: 100 characters
- Single quotes for strings
- Trailing commas in objects and arrays
- Semicolons required
- 2-space indentation

## TypeScript
- Strict mode enabled
- Use `interface` for object shapes, `type` for unions/intersections
- API response types use namespace pattern: `namespace GetServerMap { interface Response { ... } }`
- Type files located in `packages/ui/src/constants/types/`
- Never use `any` — use `unknown` and narrow types
- Prefer `const` assertions for literal types

## Imports
- Deep imports from `@pinpoint-fe/ui`: `@pinpoint-fe/ui/src/constants`, `@pinpoint-fe/ui/src/hooks`, etc.
- Group imports: React/external libs first, then `@pinpoint-fe/*`, then relative
- Use named exports; avoid default exports except for pages and lazy-loaded components

## Naming Conventions
- Components: PascalCase (`ServerMapCore.tsx`)
- Hooks: camelCase with `use` prefix (`useGetServerMapDataV2`)
- API hooks: `useGet` prefix for GET, `usePost` prefix for POST, `usePatch` for PATCH
- Atoms: camelCase with `Atom` suffix (`serverMapDataAtom`)
- Constants: SCREAMING_SNAKE_CASE for enums/config keys, camelCase for objects
- Utility functions: camelCase (`formatNumber`, `getDateRange`)
- Test files: `*.test.ts` or `*.test.tsx` co-located with source

## ESLint
- Flat config (`eslint.config.js`) with TypeScript-ESLint + Prettier
- React Hooks rules enforced (exhaustive deps)
- Do not disable ESLint rules without clear justification in a comment
