import { ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { QueryClient } from '@tanstack/react-query';

function isServerErrorResponse(body: unknown): body is ErrorResponse {
  const o = body as Record<string, unknown>;
  return (
    o != null &&
    typeof o === 'object' &&
    typeof o.status === 'number' &&
    (typeof o.detail === 'string' || typeof o.title === 'string')
  );
}

export const queryFn = (url: string) => async () => {
  const response = await fetch(url);

  if (!response.ok) {
    let body: unknown;
    try {
      body = await response.json();
    } catch {
      throw new Error(
        `Request failed with status ${response.status}. An error occurred while fetching the data.`,
      );
    }

    if (isServerErrorResponse(body)) {
      const serverError = body;
      const err = new Error(
        serverError.detail || serverError.title || 'An error occurred while fetching the data.',
      ) as Error & ErrorResponse;
      Object.assign(err, serverError);
      err.message = serverError.detail || serverError.title || err.message;
      throw err;
    }

    const detail =
      typeof (body as Record<string, unknown>)?.detail === 'string'
        ? (body as Record<string, unknown>).detail
        : undefined;
    throw new Error((detail as string) || 'An error occurred while fetching the data.');
  }
  return response.json();
};

export const queryClient = new QueryClient({
  defaultOptions: { queries: { refetchOnWindowFocus: false, staleTime: 3000 } },
});
