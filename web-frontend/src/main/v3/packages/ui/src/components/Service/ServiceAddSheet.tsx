import React from 'react';
import { useTranslation } from 'react-i18next';
import { useAtomValue, useSetAtom } from 'jotai';
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
import {
  isReservedServiceName,
  selectedServiceAtom,
  servicesAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { END_POINTS, GetServices } from '@pinpoint-fe/ui/src/constants';

export interface ServiceAddSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const ServiceAddSheet = ({ open, onOpenChange }: ServiceAddSheetProps) => {
  const { t } = useTranslation();
  const [name, setName] = React.useState('');
  const toast = useReactToastifyToast();
  const setSelectedService = useSetAtom(selectedServiceAtom);
  const services = useAtomValue(servicesAtom);
  const setServices = useSetAtom(servicesAtom);
  const { mutate, isPending } = usePostService({
    onSuccess: (_res, variables) => {
      toast.success(t('CONFIGURATION.SERVICE_SETTING.ADD_SUCCESS'));
      queryClient.setQueryData<GetServices.Response>([END_POINTS.SERVICES], (prev) =>
        prev && !prev.includes(variables.serviceName) ? [...prev, variables.serviceName] : prev,
      );
      setServices((prev) =>
        prev && !prev.includes(variables.serviceName) ? [...prev, variables.serviceName] : prev,
      );
      setSelectedService(variables.serviceName);
      queryClient.invalidateQueries({ queryKey: [END_POINTS.SERVICES] });
      onOpenChange(false);
    },
  });

  React.useEffect(() => {
    if (!open) setName('');
  }, [open]);

  const trimmed = name.trim();
  const isReserved = !!trimmed && isReservedServiceName(trimmed);
  const isDuplicate =
    !!trimmed &&
    !isReserved &&
    !!services?.some((s) => s.toLowerCase() === trimmed.toLowerCase());
  const errorMessage = isReserved
    ? t('CONFIGURATION.SERVICE_SETTING.RESERVED_NAME', { name: trimmed })
    : isDuplicate
      ? t('CONFIGURATION.SERVICE_SETTING.DUPLICATE_NAME', { name: trimmed })
      : '';

  const handleSave = () => {
    if (!trimmed || isReserved || isDuplicate) return;
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
            {t('CONFIGURATION.SERVICE_SETTING.ADD_SERVICE_TITLE')}
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
              <Label htmlFor="service-name">
                {t('CONFIGURATION.SERVICE_SETTING.LABEL.NAME')}
              </Label>
              <Input
                id="service-name"
                placeholder={t('CONFIGURATION.SERVICE_SETTING.SERVICE_NAME_PLACEHOLDER')}
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isPending}
                aria-invalid={!!errorMessage}
              />
              <p className="min-h-4 text-xs text-status-fail">{errorMessage}</p>
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isPending}>
                {t('COMMON.CANCEL')}
              </Button>
              <Button
                onClick={handleSave}
                disabled={!trimmed || isReserved || isDuplicate || isPending}
              >
                {t('COMMON.SAVE')}
              </Button>
            </div>
          </div>
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
};
