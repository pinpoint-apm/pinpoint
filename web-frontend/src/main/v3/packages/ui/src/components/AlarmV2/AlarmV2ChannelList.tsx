import { ErrorBoundary } from '@pinpoint-fe/ui';
import {
  AlarmV2ChannelListFetcher,
  AlarmV2ChannelListFetcherProps,
} from './AlarmV2ChannelListFetcher';

export type AlarmV2ChannelListProps = AlarmV2ChannelListFetcherProps;

export const AlarmV2ChannelList = ({ ...props }: AlarmV2ChannelListProps) => {
  return (
    <ErrorBoundary>
      <AlarmV2ChannelListFetcher {...props} />
    </ErrorBoundary>
  );
};
