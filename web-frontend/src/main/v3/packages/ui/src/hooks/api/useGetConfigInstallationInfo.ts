import { useQuery } from '@tanstack/react-query';
import { ConfigInstallationInfo, END_POINTS } from '@pinpoint-fe/ui/src/constants';

export const useGetConfigInstallationInfo = () => {
  return useQuery<ConfigInstallationInfo.Response>({
    queryKey: [END_POINTS.CONFIG_INSTALLATION_INFO],
    queryFn: async () => {
      const res = await fetch(`${END_POINTS.CONFIG_INSTALLATION_INFO}`);
      if (!res.ok) {
        const errorData = await res.json();
        throw errorData;
      }
      return res.json();
    },
  });
};
