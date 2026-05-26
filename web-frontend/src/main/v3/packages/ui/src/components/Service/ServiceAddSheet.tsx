import React from 'react';
import { Cross2Icon } from '@radix-ui/react-icons';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetClose,
  SheetDescription,
} from '@pinpoint-fe/ui/src/components/ui/sheet';
import { Separator } from '@pinpoint-fe/ui/src/components/ui/separator';
import { ScrollArea } from '@pinpoint-fe/ui/src/components/ui/scroll-area';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { Input } from '@pinpoint-fe/ui/src/components/ui/input';
import { Label } from '@pinpoint-fe/ui/src/components/ui/label';
import { useReactToastifyToast } from '@pinpoint-fe/ui/src/components/Toast';
import { usePostService, queryClient } from '@pinpoint-fe/ui/src/hooks';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

export interface ServiceAddSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const ServiceAddSheet = ({ open, onOpenChange }: ServiceAddSheetProps) => {
  const [name, setName] = React.useState('');
  const toast = useReactToastifyToast();
  const { mutate, isPending } = usePostService({
    onSuccess: () => {
      toast.success('Service added.');
      queryClient.invalidateQueries({ queryKey: [END_POINTS.SERVICES] });
      onOpenChange(false);
    },
  });

  React.useEffect(() => {
    if (!open) setName('');
  }, [open]);

  const handleSave = () => {
    const trimmed = name.trim();
    if (!trimmed) return;
    mutate({ serviceName: trimmed });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        hideClose
        className="flex flex-col w-full gap-0 p-0 px-0 md:max-w-full md:w-2/5"
      >
        <SheetHeader className="px-4 bg-secondary/50">
          <SheetTitle className="relative flex items-center justify-between h-16 gap-1 font-medium">
            Add Service
            <SheetClose>
              <Cross2Icon className="w-4 h-4" />
            </SheetClose>
          </SheetTitle>
          <SheetDescription />
        </SheetHeader>
        <Separator />
        <ScrollArea>
          <div className="flex flex-col gap-4 p-6">
            <div className="flex flex-col gap-2">
              <Label htmlFor="service-name">Name</Label>
              <Input
                id="service-name"
                placeholder="Enter service name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isPending}
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isPending}>
                Cancel
              </Button>
              <Button onClick={handleSave} disabled={!name.trim() || isPending}>
                Save
              </Button>
            </div>
          </div>
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
};
