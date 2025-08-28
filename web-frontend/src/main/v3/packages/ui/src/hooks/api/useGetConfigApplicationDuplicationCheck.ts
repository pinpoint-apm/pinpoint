import { useQuery } from '@tanstack/react-query';
import { ConfigApplicationDuplicationCheck, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<ConfigApplicationDuplicationCheck.Parameters>) => {
  if (queryParams.applicationName) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigApplicationDuplicationCheck = ({
  applicationName,
}: {
  applicationName: string;
}) => {
  const queryParams = {
    applicationName,
  };
  const queryString = getQueryString(queryParams);

  return useQuery<ConfigApplicationDuplicationCheck.Response>({
    queryKey: [END_POINTS.CONFIG_APPLICATION_DUPLICATION_CHECK, queryString],
    queryFn: queryFn(`${END_POINTS.CONFIG_APPLICATION_DUPLICATION_CHECK}${queryString}`),
    enabled: !!queryString,
    retry: false,
  });
};
