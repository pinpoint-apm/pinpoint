import * as React from 'react';
import * as AccordionPrimitive from '@radix-ui/react-accordion';
import { ChevronDownIcon } from '@radix-ui/react-icons';

import { cn } from '../../lib/utils';

const Accordion = AccordionPrimitive.Root;

const AccordionItem = ({
  className,
  ref,
  ...props
}: React.ComponentPropsWithoutRef<typeof AccordionPrimitive.Item> & {
  ref?: React.Ref<React.ElementRef<typeof AccordionPrimitive.Item>>;
}) => <AccordionPrimitive.Item ref={ref} className={cn('border-b', className)} {...props} />;
AccordionItem.displayName = 'AccordionItem';

const AccordionTrigger = ({
  className,
  children,
  ref,
  ...props
}: React.ComponentPropsWithoutRef<typeof AccordionPrimitive.Trigger> & {
  ref?: React.Ref<React.ElementRef<typeof AccordionPrimitive.Trigger>>;
}) => (
  <AccordionPrimitive.Header className="flex">
    <AccordionPrimitive.Trigger
      ref={ref}
      className={cn(
        'flex flex-1 items-center justify-between py-4 text-sm font-medium transition-all hover:underline [&[data-state=open]>svg]:rotate-180',
        className,
      )}
      {...props}
    >
      {children}
      <ChevronDownIcon className="w-4 h-4 transition-transform duration-200 shrink-0 text-muted-foreground" />
    </AccordionPrimitive.Trigger>
  </AccordionPrimitive.Header>
);
AccordionTrigger.displayName = AccordionPrimitive.Trigger.displayName;

const AccordionContent = ({
  contentWrapperClassName,
  className,
  children,
  ref,
  ...props
}: React.ComponentPropsWithoutRef<typeof AccordionPrimitive.Content> & {
  contentWrapperClassName?: string;
  ref?: React.Ref<React.ElementRef<typeof AccordionPrimitive.Content>>;
}) => (
  <AccordionPrimitive.Content
    ref={ref}
    className={cn(
      'overflow-hidden text-sm data-[state=closed]:animate-accordion-up data-[state=open]:animate-accordion-down',
      contentWrapperClassName,
    )}
    {...props}
  >
    <div className={cn('pb-4 pt-0', className)}>{children}</div>
  </AccordionPrimitive.Content>
);
AccordionContent.displayName = AccordionPrimitive.Content.displayName;

export { Accordion, AccordionItem, AccordionTrigger, AccordionContent };
