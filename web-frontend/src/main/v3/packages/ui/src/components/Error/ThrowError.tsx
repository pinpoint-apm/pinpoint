import { ErrorLike } from '@pinpoint-fe/ui/src/constants';

export const ThrowError = ({ error }: { error: Error | ErrorLike }) => {
  const err = new Error(
    (error as ErrorLike).detail || (error as ErrorLike).title || 'An error occurred.',
  ) as Error;
  Object.assign(err, error);
  err.message = (error as ErrorLike).detail || (error as ErrorLike).title || err.message;
  throw err;
};
