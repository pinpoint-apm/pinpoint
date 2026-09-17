import { useGetApplicationList } from '@pinpoint-fe/ui/src/hooks';
import { ApplicationCombinedList } from '../../components/Application/ApplicationCombinedList';
import { AlarmV2Page } from './AlarmV2';

export const ServiceAlarmPage = () => {
  const { data: applications } = useGetApplicationList();

  return (
    <AlarmV2Page
      hasPermission
      applications={applications}
      ApplicationList={ApplicationCombinedList}
    />
  );
};
