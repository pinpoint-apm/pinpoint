import { useTranslation } from 'react-i18next';
import { Button } from '../ui';
import {
  ErrorBoundary as ErrorBoundaryComponent,
  ErrorBoundaryProps as ErrorBoundaryPropsWithRender,
} from 'react-error-boundary';
import { ErrorDetailDialog } from './ErrorDetailDialog';
import { toast } from '../Toast';
import { ErrorResponse } from '@pinpoint-fe/constants';
import { ErrorToast } from './ErrorToast';

export type ErrorBoundaryProps = Partial<ErrorBoundaryPropsWithRender>;

export const ErrorBoundary = ({ fallbackRender, children }: ErrorBoundaryProps) => {
  const { t } = useTranslation();
  return (
    <ErrorBoundaryComponent
      onError={(props) => {
        const error = props as unknown as ErrorResponse;
        toast.error(<ErrorToast error={error} />, {
          bodyClassName: '!items-start',
          autoClose: false,
        });
      }}
      fallbackRender={
        (({ error, resetErrorBoundary }) => {
          return (
            <div className="flex flex-col items-center justify-center w-full h-full gap-5 p-3">
              <div className="text-center">
                <p className="mb-2 text-sm">{t('COMMON.SOMETHING_WENT_WRONG')}</p>
                <ErrorDetailDialog error={error} />
              </div>
              <div className="flex gap-2">
                <Button className="text-xs" variant="outline" onClick={resetErrorBoundary}>
                  {t('COMMON.TRY_AGAIN')}
                </Button>
                <Button
                  className="text-xs"
                  variant="outline"
                  onClick={() => {
                    location.reload();
                  }}
                >
                  {t('COMMON.PAGE_REFRESH')}
                </Button>
              </div>
            </div>
          );
        }) || ((param) => fallbackRender?.(param))
      }
    >
      {children}
    </ErrorBoundaryComponent>
  );
};
