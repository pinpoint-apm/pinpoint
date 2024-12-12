import { useNavigate } from 'react-router-dom';
import { useSystemMetricSearchParameters } from '@pinpoint-fe/ui/hooks';
import {
  getSystemMetricPath,
  convertParamsToQueryString,
  getFormattedDateRange,
} from '@pinpoint-fe/utils';
import { HostSearchList } from '../../Host';

export interface SystemMetricSidebarProps {}

export const SystemMetricSidebar = () => {
  const navigate = useNavigate();
  const { dateRange, hostGroupName, hostName } = useSystemMetricSearchParameters();
  return (
    <div className="w-60 min-w-[15rem] border-r-1 h-full">
      <HostSearchList
        selectedHost={hostName}
        onClickHost={(host) => {
          navigate(
            `${getSystemMetricPath(hostGroupName)}?${convertParamsToQueryString({
              ...getFormattedDateRange(dateRange),
              hostName: host,
            })}`,
          );
        }}
      />
    </div>
  );
};
