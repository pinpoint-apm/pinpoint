import { ErrorResponse } from '@pinpoint-fe/constants';
import { QueryClient } from '@tanstack/react-query';

export const queryFn = (url: string) => async () => {
  const response = await fetch(url);

  if (!response.ok) {
    const error: ErrorResponse = await response.json();

    if (error.data) {
      // Server API error
      throw error;
    } else {
      // Network error
      throw new Error('An error occurred while fetching the data.');
    }
  }
  return response.json();
};

export const queryClient = new QueryClient({
  defaultOptions: { queries: { refetchOnWindowFocus: false, staleTime: 3000 } },
});
