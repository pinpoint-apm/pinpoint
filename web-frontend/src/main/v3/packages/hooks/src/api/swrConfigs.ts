import { ErrorResponse } from '@pinpoint-fe/constants';
import { SWRConfiguration } from 'swr';

export const swrConfigs: SWRConfiguration = {
  suspense: true,
  revalidateIfStale: false,
  revalidateOnFocus: false,
  fetcher: async (url, params) => {
    const queryParamString = new URLSearchParams(params)?.toString();
    const urlWithQueryParams = queryParamString ? `${url}?${queryParamString}` : url;

    const response = await fetch(urlWithQueryParams);

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
  },
};

type MutateMethod = 'POST' | 'PUT' | 'DELETE';
export const getMutateFetcher =
  <T>(method: MutateMethod) =>
  async (url: string, { arg }: { arg: T }) => {
    return fetch(url, {
      method,
      body: JSON.stringify(arg),
      headers: {
        'Content-Type': 'application/json',
      },
    });
  };
