import useSWR from 'swr';
import { ConfigInstallationInfo, END_POINTS } from '@pinpoint-fe/ui/constants';
import { swrConfigs } from './swrConfigs';

export const useGetConfigInstallationInfo = () => {
  return useSWR<ConfigInstallationInfo.Response>(`${END_POINTS.CONFIG_INSTALLATION_INFO}`, {
    ...swrConfigs,
    suspense: false,
  });
};
