import { CgSpinner } from 'react-icons/cg';
import { Alert, AlertTitle, AlertDescription } from '../ui';
import { RxExclamationTriangle, RxCheck } from 'react-icons/rx';
import { ErrorDetailDialog } from '../Error/ErrorDetailDialog';
import { ErrorResponse } from '@pinpoint-fe/ui/constants';

export interface ApiStatusProps<T, K> {
  data: T;
  error: K;
  path: string;
  isLoading: boolean;
}

export const ApiStatus = <T, K>({ data, isLoading, error, path }: ApiStatusProps<T, K>) => {
  if (data) {
    return (
      <Alert className="w-full sm:w-2/3 lg:w-5/12" variant="success">
        <RxCheck />
        <AlertTitle className="font-semibold">Success</AlertTitle>
        <AlertDescription>{path}</AlertDescription>
      </Alert>
    );
  }
  if (isLoading) {
    return (
      <Alert className="w-full sm:w-2/3 lg:w-5/12" variant="pending">
        <CgSpinner className="animate-spin" />
        <AlertTitle className="font-semibold">Loading...</AlertTitle>
        <AlertDescription>{path}</AlertDescription>
      </Alert>
    );
  }
  if (error) {
    return (
      <Alert className="w-full sm:w-2/3 lg:w-5/12" variant="destructive">
        <RxExclamationTriangle />
        <AlertTitle className="font-semibold">Error</AlertTitle>
        <AlertDescription>{path}</AlertDescription>
        <ErrorDetailDialog error={error as unknown as ErrorResponse} />
      </Alert>
    );
  }
  return null;
};
