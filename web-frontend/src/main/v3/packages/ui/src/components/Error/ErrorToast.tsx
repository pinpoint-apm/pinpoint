import { useTranslation } from 'react-i18next';
import { ErrorLike } from '@pinpoint-fe/ui/src/constants';
import { Separator } from '../../components/ui/separator';
import { ErrorDetailDialog } from './ErrorDetailDialog';
import { cn } from '../../lib/utils';

export interface ErrorToastProps {
  error: Error | ErrorLike;
  className?: string;
}

export const ErrorToast = ({ error, className }: ErrorToastProps) => {
  const { t } = useTranslation();
  const err = error as ErrorLike;
  return (
    <div className={cn('space-y-2', className)}>
      <div>
        {err?.title ? err.title : t('COMMON.SOMETHING_WENT_WRONG')}:
        <br />
        {err?.instance && (
          <>
            <span className="font-semibold items">{err.instance}</span>
          </>
        )}
      </div>
      <Separator />
      <p className="text-xs">{err?.detail ?? error?.message}</p>
      <ErrorDetailDialog error={error} />
    </div>
  );
};
