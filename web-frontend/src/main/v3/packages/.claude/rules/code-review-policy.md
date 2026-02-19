# Code Review & Regression Prevention Policy

**This rule applies to ALL code modifications. No exceptions.**

## Mandatory Code Review Process

Every time code is modified, the following review must be performed:

### 1. Pre-Edit Analysis
- Read the file(s) being modified completely before making changes
- Identify all callers, importers, and consumers of the modified code
- Understand the existing behavior that must be preserved

### 2. Impact Analysis
- Search for all usages of modified functions, components, types, hooks, and constants
- Trace data flow: if a type changes, check all places that create or consume that type
- If modifying a hook, check all components that use it
- If modifying an atom, check all components that read or write it
- If modifying a component's props, check all parent components that render it
- If modifying an endpoint or constant, check all hooks and utilities that reference it

### 3. Post-Edit Verification
- Re-read the modified code and all affected files
- Confirm that existing function signatures, return types, and prop interfaces remain compatible
- Verify that no existing import paths are broken
- Check that default values and optional/required status of parameters are preserved
- Ensure i18n keys used elsewhere are not removed or renamed without updating all references

### 4. Regression Checklist
- [ ] All existing callers of modified functions still receive correct arguments
- [ ] All existing consumers of modified types still satisfy the type contract
- [ ] All existing renderers of modified components still pass valid props
- [ ] No exports were accidentally removed or renamed
- [ ] No query keys were changed in a way that breaks cache invalidation
- [ ] Route loaders still validate and redirect correctly
- [ ] withInitialFetch HOC still receives expected data shape

### 5. Test Awareness
- If modifying code that has tests, run the relevant tests: `yarn workspace @pinpoint-fe/ui test`
- If modifying code without tests, consider the risk and suggest adding tests for critical paths
- Never skip test verification for changes to: atoms, utility functions, API hooks, route loaders

## Scope of Review
- **Single file change**: Review the file + all direct importers
- **Cross-file change**: Review all files in the change set + their importers
- **Type/interface change**: Review ALL files that reference the type (use Grep)
- **Constant/endpoint change**: Review ALL hooks and components using the constant
