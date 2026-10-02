import { DataTableSkeleton } from '@pinpoint-fe/ui';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { useAlarmV2RuleQuery } from '@pinpoint-fe/ui/src/hooks/api';
import { AlarmV2RuleTable, AlarmV2RuleTableProps } from './AlarmV2RuleTable';

export type AlarmV2RuleListFetcherProps = Omit<AlarmV2RuleTableProps, 'data'> &
  AlarmV2Rule.Parameters;

export const AlarmV2RuleListFetcher = ({
  applicationName,
  applicationType,
  ...props
}: AlarmV2RuleListFetcherProps) => {
  const { data, isLoading, error } = useAlarmV2RuleQuery({
    applicationName,
    applicationType,
  });

  if (error && !data) {
    throw error;
  }
  if (isLoading) {
    return <DataTableSkeleton hideRowBox />;
  }

  return <AlarmV2RuleTable data={data} {...props} />;
};
