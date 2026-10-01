import { useNavigate } from 'react-router';
import { useRequestService, useSystemMetricSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  getSystemMetricPath,
  convertParamsToQueryString,
  getFormattedDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { HostSearchList } from '../../Host';

export interface SystemMetricSidebarProps {}

export const SystemMetricSidebar = () => {
  const navigate = useNavigate();
  const { dateRange, hostGroupName, hostName } = useSystemMetricSearchParameters();
  const serviceName = useRequestService();
  return (
    <div className="w-auto h-full min-w-auto">
      <HostSearchList
        selectedHost={hostName}
        onClickHost={(host) => {
          navigate(
            `${getSystemMetricPath(hostGroupName, undefined, serviceName)}?${convertParamsToQueryString(
              {
                ...getFormattedDateRange(dateRange),
                hostName: host,
              },
            )}`,
          );
        }}
      />
    </div>
  );
};
