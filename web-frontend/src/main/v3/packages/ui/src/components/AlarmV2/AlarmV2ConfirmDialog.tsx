import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@pinpoint-fe/ui/src/components/ui/alert-dialog';

export interface AlarmV2ConfirmDialogProps {
  open: boolean;
  title: React.ReactNode;
  description: React.ReactNode;
  disabled?: boolean;
  /** Red confirm button. Deletions are destructive; a save that fans out is not. */
  destructive?: boolean;
  onConfirm: () => void;
  onOpenChange: (open: boolean) => void;
}

export const AlarmV2ConfirmDialog = ({
  open,
  title,
  description,
  disabled,
  destructive = true,
  onConfirm,
  onOpenChange,
}: AlarmV2ConfirmDialogProps) => {
  const { t } = useTranslation();

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>{t('COMMON.CANCEL')}</AlertDialogCancel>
          <AlertDialogAction
            disabled={disabled}
            buttonVariant={destructive ? { variant: 'destructive' } : undefined}
            onClick={onConfirm}
          >
            {t('COMMON.CONFIRM')}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};
