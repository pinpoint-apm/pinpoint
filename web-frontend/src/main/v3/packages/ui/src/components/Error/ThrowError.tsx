import { ErrorLike } from '@pinpoint-fe/ui/src/constants';

export const ThrowError = ({ error }: { error: Error | ErrorLike }) => {
  const detail = (error as ErrorLike).detail;
  const title = (error as ErrorLike).title;
  const normalizedMessage = detail || title || (error instanceof Error ? error.message : 'An error occurred.');

  if (error instanceof Error) {
    error.message = normalizedMessage;
    throw error;
  }

  const err = new Error(normalizedMessage);
  Object.assign(err, error);
  throw err;
};
