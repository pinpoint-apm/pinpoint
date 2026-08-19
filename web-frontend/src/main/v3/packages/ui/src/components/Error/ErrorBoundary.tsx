import { useTranslation } from 'react-i18next';
import { Button } from '../ui';
import {
  ErrorBoundary as ErrorBoundaryComponent,
  ErrorBoundaryProps as ErrorBoundaryPropsWithRender,
} from 'react-error-boundary';
import { ErrorDetailDialog } from './ErrorDetailDialog';
import { ErrorLike } from '@pinpoint-fe/ui/src/constants';
import { useSearchParameters } from '@pinpoint-fe/ui/src/hooks';

export type ErrorBoundaryProps = Partial<ErrorBoundaryPropsWithRender> & {
  errorMessage?: React.ReactNode | ((message?: string) => React.ReactNode);
};

export const ErrorBoundary = ({
  fallbackRender,
  children,
  errorMessage,
  resetKeys,
}: ErrorBoundaryProps) => {
  const { t } = useTranslation();
  const { search } = useSearchParameters();

  return (
    // 에러 토스트는 QueryCache/MutationCache의 글로벌 onError(reactQueryHelper)가
    // 단일 출처로 담당한다. ErrorBoundary는 인라인 fallback UI만 렌더한다(토스트 중복 방지).
    <ErrorBoundaryComponent
      resetKeys={[search, ...(resetKeys || [])]}
      fallbackRender={({ error, resetErrorBoundary }) => {
        if (fallbackRender) {
          return fallbackRender({ error, resetErrorBoundary });
        }
        // react-error-boundary v6 부터 error 가 unknown 이다(무엇이든 throw 될 수 있으므로).
        // 이 앱이 throw 하는 것은 Error 아니면 ProblemDetail(ErrorLike) 둘 뿐이라 좁혀서 쓴다.
        const err = error as (ErrorLike & Error) | undefined;
        return (
          <div className="flex flex-col gap-5 justify-center items-center p-3 w-full h-full">
            <div className="w-full text-center max-w-[28rem]">
              {errorMessage ? (
                typeof errorMessage === 'function' ? (
                  errorMessage(err?.detail ?? err?.message ?? err?.title)
                ) : (
                  errorMessage
                )
              ) : (
                <p className="mb-2 text-sm truncate">
                  {err?.detail ?? err?.message ?? err?.title ?? t('COMMON.SOMETHING_WENT_WRONG')}
                </p>
              )}
              <ErrorDetailDialog error={err as ErrorLike} />
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
      }}
    >
      {children}
    </ErrorBoundaryComponent>
  );
};
