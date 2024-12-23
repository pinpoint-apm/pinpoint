import useSWR from 'swr';
import { ConfigApplicationDuplicationCheck, END_POINTS } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';

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

  return useSWR<ConfigApplicationDuplicationCheck.Response>(
    queryString ? `${END_POINTS.CONFIG_APPLICATION_DUPLICATION_CHECK}${queryString}` : null,
    {
      ...swrConfigs,
      suspense: false,
    },
  );
};
