import { AlarmTable, AlarmTableProps } from './AlarmTable';
import { useAlarmRuleQuery } from '@pinpoint-fe/ui/src/hooks';

export interface AlarmListFetcherProps extends AlarmTableProps {
  applicationName?: string;
}

export const AlarmListFetcher = ({ applicationName, ...props }: AlarmListFetcherProps) => {
  const { data } = useAlarmRuleQuery({ applicationName, suspense: true });
  return <AlarmTable data={data} {...props} />;
};
