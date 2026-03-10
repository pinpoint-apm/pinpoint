import { ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { MutationCache, QueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { ErrorToast } from '../../components/Error/ErrorToast';

declare module '@tanstack/react-query' {
  interface Register {
    mutationMeta: {
      ignoreGlobalError?: boolean;
    };
  }
}

function isServerErrorResponse(body: unknown): body is ErrorResponse {
  const o = body as Record<string, unknown>;
  return (
    o != null &&
    typeof o === 'object' &&
    typeof o.status === 'number' &&
    (typeof o.detail === 'string' || typeof o.title === 'string')
  );
}

export async function parseResponseError(response: Response): Promise<never> {
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
      : typeof (body as Record<string, unknown>)?.message === 'string'
        ? (body as Record<string, unknown>).message
        : undefined;
  throw new Error((detail as string) || 'An error occurred while fetching the data.');
}

export const queryFn = (url: string) => async () => {
  const response = await fetch(url);

  if (!response.ok) {
    await parseResponseError(response);
  }
  return response.json();
};

const mutationCache = new MutationCache({
  onError: (error, _variables, _context, mutation) => {
    if (mutation.meta?.ignoreGlobalError) return;
    if (mutation.options.onError) return;

    toast.error(<ErrorToast error={error as Error} />, {
      className: 'pointer-events-auto',
      bodyClassName: '!items-start',
      autoClose: false,
    });
  },
});

export const queryClient = new QueryClient({
  mutationCache,
  defaultOptions: { queries: { refetchOnWindowFocus: false, staleTime: 3000 } },
});
