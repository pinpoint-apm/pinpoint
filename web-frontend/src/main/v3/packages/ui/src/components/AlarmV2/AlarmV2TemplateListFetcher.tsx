import { DataTableSkeleton } from '@pinpoint-fe/ui';
import { useAlarmV2TemplateQuery } from '@pinpoint-fe/ui/src/hooks/api';
import { AlarmV2TemplateTable, AlarmV2TemplateTableProps } from './AlarmV2TemplateTable';

export type AlarmV2TemplateListFetcherProps = Omit<AlarmV2TemplateTableProps, 'data'>;

export const AlarmV2TemplateListFetcher = (props: AlarmV2TemplateListFetcherProps) => {
  const { data, isLoading, error } = useAlarmV2TemplateQuery();

  if (error && !data) {
    throw error;
  }
  if (isLoading) {
    return <DataTableSkeleton hideRowBox />;
  }

  return <AlarmV2TemplateTable data={data} {...props} />;
};
