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
} from '../../../components/ui/alert-dialog';

export function OpenTelemetryAlertDialog({
  open,
  onCancel,
  onContinue,
}: {
  open?: boolean;
  onCancel?: () => void;
  onContinue?: () => void;
}) {
  const { t } = useTranslation();

  return (
    <AlertDialog open={open} onOpenChange={onCancel}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{t('COMMON.CONFIRM_LEAVE_PAGE')}</AlertDialogTitle>
          <AlertDialogDescription>{t('COMMON.CHANGE_NOT_SAVED')}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel onClick={onCancel}>{t('COMMON.CANCEL')}</AlertDialogCancel>
          <AlertDialogAction
            buttonVariant={{
              variant: 'destructive',
            }}
            onClick={onContinue}
          >
            {t('COMMON.CONTINUE')}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
