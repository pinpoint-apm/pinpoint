import { DataTableSkeleton } from '@pinpoint-fe/ui';
import { useAlarmV2ChannelQuery } from '@pinpoint-fe/ui/src/hooks/api';
import { AlarmV2ChannelTable, type AlarmV2ChannelTableProps } from './AlarmV2ChannelTable';

export type AlarmV2ChannelListFetcherProps = Omit<AlarmV2ChannelTableProps, 'data'>;

export const AlarmV2ChannelListFetcher = ({ ...props }: AlarmV2ChannelListFetcherProps) => {
  const { data, isLoading, error } = useAlarmV2ChannelQuery();

  if (error && !data) {
    throw error;
  }
  if (isLoading) {
    return <DataTableSkeleton hideRowBox />;
  }

  return <AlarmV2ChannelTable data={data} {...props} />;
};
