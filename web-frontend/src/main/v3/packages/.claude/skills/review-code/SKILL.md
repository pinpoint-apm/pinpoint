---
name: review-code
description: Review code changes against Pinpoint project conventions. Use for pre-commit or PR review.
argument-hint: "[file-or-directory]"
context: fork
agent: Explore
allowed-tools: Read, Glob, Grep, Bash(git diff*, git log*, git show*)
---

# Code Review

Review the code at `$ARGUMENTS` (or recent git changes if no path given).

## Review Checklist

### 1. Architecture
- [ ] Code in correct package (apps/web for routing, packages/ui for everything else)
- [ ] Page follows withInitialFetch + Layout wrapper pattern
- [ ] API hooks use shared queryFn from reactQueryHelper
- [ ] State management uses correct layer (URL → Jotai → React Query)

### 2. Code Style
- [ ] Prettier formatting (100 char, single quotes, trailing commas, semicolons)
- [ ] TypeScript strict compliance (no `any`, proper type narrowing)
- [ ] Import order: external → @pinpoint-fe/* → relative
- [ ] Deep imports: `@pinpoint-fe/ui/src/constants` not `@pinpoint-fe/ui`

### 3. Components
- [ ] UI primitives follow shadcn/ui pattern (Radix + CVA + Tailwind)
- [ ] Domain components compose from UI primitives
- [ ] `cn()` used for className merging
- [ ] No inline styles or CSS modules

### 4. Data Fetching
- [ ] Endpoints defined in EndPoints.ts
- [ ] Types defined with namespace pattern
- [ ] `enabled` option prevents premature queries
- [ ] Error handling via ProblemDetail pattern

### 5. i18n
- [ ] All user-facing strings use `t()` function
- [ ] Keys added to both en.json and ko.json
- [ ] Key naming follows SECTION.KEY_NAME convention

### 6. Common Pitfalls
- [ ] No missing React hook dependencies
- [ ] No memory leaks (cleanup in useEffect)
- [ ] Lazy loading used for new pages
- [ ] No hardcoded API paths (use END_POINTS constants)

Output findings organized by severity: Critical > Warning > Suggestion.
