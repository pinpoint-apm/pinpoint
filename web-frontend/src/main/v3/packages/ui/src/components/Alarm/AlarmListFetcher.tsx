import { AlarmTable, AlarmTableProps } from './AlarmTable';
import { useAlarmRuleQuery } from '@pinpoint-fe/ui/src/hooks';

export interface AlarmListFetcherProps extends AlarmTableProps {
  applicationId?: string;
}

export const AlarmListFetcher = ({ applicationId, ...props }: AlarmListFetcherProps) => {
  const { data } = useAlarmRuleQuery({ applicationId, suspense: true });
  return <AlarmTable data={data} {...props} />;
};
