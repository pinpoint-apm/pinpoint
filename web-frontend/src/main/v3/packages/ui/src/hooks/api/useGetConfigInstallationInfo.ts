import { useQuery } from '@tanstack/react-query';
import { ConfigInstallationInfo, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';

export const useGetConfigInstallationInfo = () => {
  return useQuery<ConfigInstallationInfo.Response>({
    queryKey: [END_POINTS.CONFIG_INSTALLATION_INFO],
    queryFn: queryFn(END_POINTS.CONFIG_INSTALLATION_INFO),
  });
};
