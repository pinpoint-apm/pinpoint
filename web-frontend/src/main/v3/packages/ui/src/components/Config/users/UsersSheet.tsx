import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  Separator,
  ScrollArea,
  SheetClose,
  SheetDescription,
} from '../../../components';
import { t } from 'i18next';
import { Cross2Icon } from '@radix-ui/react-icons';
import { cn } from '../../../lib';

export interface UsersSheetProps {
  open?: boolean;
  isEdit?: boolean;
  children?: React.ReactNode;
  onOpenChange?: (open: boolean) => void;
}

export function UsersSheet({ open, onOpenChange, children, isEdit }: UsersSheetProps) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        hideClose
        className="flex flex-col w-full gap-0 p-0 px-0 md:max-w-full md:w-2/5"
      >
        <SheetHeader className="px-4 bg-secondary/50">
          <SheetTitle className="relative flex items-center justify-between h-16 gap-1 font-medium">
            {isEdit
              ? t('CONFIGURATION.USERS.USER_INFO_TITLE')
              : t('CONFIGURATION.USERS.USER_ADD_TITLE')}
            <SheetClose
              className={cn({
                'absolute left-0 top-1 text-muted-foreground': false,
              })}
            >
              <Cross2Icon className="w-4 h-4" />
            </SheetClose>
          </SheetTitle>
          <SheetDescription />
        </SheetHeader>
        <Separator />
        <ScrollArea>{children}</ScrollArea>
      </SheetContent>
    </Sheet>
  );
}
