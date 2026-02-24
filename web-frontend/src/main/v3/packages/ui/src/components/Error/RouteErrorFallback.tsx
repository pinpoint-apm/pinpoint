import { useRouteError } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '../ui';
import { ErrorDetailDialog } from './ErrorDetailDialog';
import { ErrorLike } from '../../constants';

export const RouteErrorFallback = () => {
  const error = useRouteError();
  const { t } = useTranslation();

  const isChunkLoadError =
    error instanceof Error &&
    (error.name === 'ChunkLoadError' ||
      error.message?.includes('Failed to fetch dynamically imported module') ||
      error.message?.includes('Loading chunk'));

  return (
    <div className="flex flex-col items-center justify-center w-full h-full gap-5 p-3">
      <div className="w-full text-center max-w-[28rem]">
        <p className="mb-2 text-sm truncate">
          {!isChunkLoadError && error instanceof Error
            ? ((error as ErrorLike)?.detail ??
              error?.message ??
              (error as ErrorLike)?.title ??
              t('COMMON.SOMETHING_WENT_WRONG'))
            : t('COMMON.SOMETHING_WENT_WRONG')}
        </p>
        {error instanceof Error && <ErrorDetailDialog error={error} />}
      </div>
      <div className="flex gap-2">
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
};
