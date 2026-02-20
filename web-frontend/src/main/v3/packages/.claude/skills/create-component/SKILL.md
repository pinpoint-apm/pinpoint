---
name: create-component
description: Create a new React component following the project's shadcn/ui + Tailwind pattern. Use for UI primitives or domain components.
argument-hint: "[component-name] [description]"
allowed-tools: Read, Glob, Grep, Edit, Write
---

# Create a New Component

Create a component named `$0` with description: `$ARGUMENTS`.

## 1. Determine Component Type

### UI Primitive (packages/ui/src/components/ui/)
For reusable, generic UI components (button variants, dialogs, inputs, etc.):
- Follow shadcn/ui pattern: Radix UI + CVA + Tailwind
- Use `cn()` from `@pinpoint-fe/ui/src/lib/utils`
- Support `asChild` via Radix Slot where appropriate
- Use `React.forwardRef` for ref forwarding
- Define variants with CVA (class-variance-authority)

### Domain Component (packages/ui/src/components/{DomainName}/)
For feature-specific components (ServerMap, ErrorAnalysis, etc.):
- Create a subdirectory under `packages/ui/src/components/`
- Compose from UI primitives
- Use React Query hooks for data fetching
- Use Jotai atoms for shared state
- Wrap async content in Suspense + ErrorBoundary

## 2. Read Existing Examples
- For UI primitives: read `packages/ui/src/components/ui/button.tsx` or `dialog.tsx`
- For domain components: read an existing domain component similar to the target

## 3. Create the Component

### UI Primitive Template
```tsx
import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';

const componentVariants = cva('base-classes', {
  variants: { variant: { default: '...', outline: '...' }, size: { default: '...', sm: '...' } },
  defaultVariants: { variant: 'default', size: 'default' },
});

export interface ComponentProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof componentVariants> {}

const Component = React.forwardRef<HTMLDivElement, ComponentProps>(
  ({ className, variant, size, ...props }, ref) => (
    <div ref={ref} className={cn(componentVariants({ variant, size, className }))} {...props} />
  ),
);
Component.displayName = 'Component';
export { Component, componentVariants };
```

### Domain Component Template
```tsx
import React from 'react';
import { useTranslation } from 'react-i18next';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';

export interface MyComponentProps {
  className?: string;
  // typed props
}

export const MyComponent = ({ className, ...props }: MyComponentProps) => {
  const { t } = useTranslation();
  return <div className={cn('...', className)}>...</div>;
};
```

## 4. Styling Rules
- Tailwind CSS only — no inline styles, no CSS modules
- Use CSS variables for theming: `var(--ui-primary)`, `var(--ui-border)`
- Use `cn()` for all className composition
- Responsive design with Tailwind breakpoints
