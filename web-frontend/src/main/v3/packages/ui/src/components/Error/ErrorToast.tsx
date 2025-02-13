import { useTranslation } from 'react-i18next';
import { ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { Separator } from '../../components/ui/separator';
import { ErrorDetailDialog } from './ErrorDetailDialog';
import { cn } from '../../lib/utils';

export interface ErrorToastProps {
  error: ErrorResponse;
  title?: string;
  className?: string;
}

export const ErrorToast = ({ error, title, className }: ErrorToastProps) => {
  const { t } = useTranslation();
  return (
    <div className={cn('space-y-2', className)}>
      <div>
        {title ? title : t('COMMON.SOMETHING_WENT_WRONG')}:
        <br />
        {error?.instance && (
          <>
            <span className="font-semibold items">{error.instance}</span>
          </>
        )}
      </div>
      <Separator />
      <p className="text-xs">{error?.message}</p>
      <ErrorDetailDialog error={error} />
    </div>
  );
};
