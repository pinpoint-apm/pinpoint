import React from 'react';
import { Cross2Icon } from '@radix-ui/react-icons';
import { Separator } from '@pinpoint-fe/ui/src/components/ui/separator';
import { ScrollArea } from '@pinpoint-fe/ui/src/components/ui/scroll-area';
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@pinpoint-fe/ui/src/components/ui/sheet';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';

export interface AlarmV2SheetProps {
  open: boolean;
  title: React.ReactNode;
  children: React.ReactNode;
  bodyClassName?: string;
  onOpenChange: (open: boolean) => void;
}

export const AlarmV2Sheet = ({
  open,
  title,
  children,
  bodyClassName,
  onOpenChange,
}: AlarmV2SheetProps) => {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        hideClose
        className="flex flex-col w-full gap-0 p-0 px-0 md:max-w-full md:w-2/5"
      >
        <SheetHeader className="px-4 bg-secondary/50">
          <SheetTitle className="relative flex items-center justify-between h-24 gap-1 pt-8 pb-6">
            {title}
            <SheetClose>
              <Cross2Icon className="w-4 h-4" />
            </SheetClose>
          </SheetTitle>
        </SheetHeader>
        <Separator />
        <ScrollArea>
          <div className={cn('p-4', bodyClassName)}>{children}</div>
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
};
