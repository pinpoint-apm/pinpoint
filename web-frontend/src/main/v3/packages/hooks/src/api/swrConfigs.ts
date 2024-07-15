import { ErrorResponse } from '@pinpoint-fe/constants';
import { SWRConfiguration } from 'swr';

export const parseErrorResponse = async (response: Response) => {
  const contentType = response.headers.get('content-type');

  if (contentType?.includes('json')) {
    return await response.json();
  } else if (contentType?.includes('text/html')) {
    const htmlText = await response.text();
    const parser = new DOMParser();
    const DOM = parser.parseFromString(htmlText, 'text/html');
    const titleElement = DOM.querySelector('h1');
    const traceElement = DOM.querySelector('pre');
    const title = titleElement?.textContent || '';
    const trace = traceElement?.textContent || '';

    return {
      data: {},
      instance: title,
      trace,
    } as ErrorResponse;
  } else {
    return new Error('Unsupported content type.');
  }
};

export const swrConfigs: SWRConfiguration = {
  suspense: true,
  revalidateIfStale: false,
  revalidateOnFocus: false,
  fetcher: async (url, params) => {
    const queryParamString = new URLSearchParams(params)?.toString();
    const urlWithQueryParams = queryParamString ? `${url}?${queryParamString}` : url;

    const response = await fetch(urlWithQueryParams);

    if (!response.ok) {
      const error = await parseErrorResponse(response);

      if (error.data) {
        // Server API error
        throw { ...error, url };
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
