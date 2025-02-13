import { Webhook, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useQuery, useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<Webhook.Parameters>) => {
  if (queryParams.ruleId || queryParams.applicationId) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

interface UseGetWebhookProps extends Webhook.Parameters {
  disableFetch?: boolean;
  suspense?: boolean;
}

export const useGetWebhook = ({ disableFetch, suspense, ...params }: UseGetWebhookProps) => {
  const queryString = getQueryString(params);
  const query = suspense ? useSuspenseQuery : useQuery;

  const { data, isLoading, refetch } = query<Webhook.Response | null>({
    queryKey: [END_POINTS.WEBHOOK, queryString],
    queryFn:
      !!queryString && !disableFetch ? queryFn(`${END_POINTS.WEBHOOK}${queryString}`) : () => null,
    staleTime: 30000,
  });

  return { data, isLoading, refetch };
};
