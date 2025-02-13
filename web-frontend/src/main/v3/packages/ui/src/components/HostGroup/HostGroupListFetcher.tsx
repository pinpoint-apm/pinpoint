import { VirtualList, VirtualListProps, ListItemSkeleton } from '../VirtualList';
import { useGetSystemMetricHostGroupData } from '@pinpoint-fe/ui/src/hooks';
import { Button } from '../ui';
import { LuRotateCw } from 'react-icons/lu';

export interface HostGroupListFetcherProps extends VirtualListProps<string> {}

export const HostGroupListFetcher = (props: HostGroupListFetcherProps) => {
  const { data, mutate, isValidating } = useGetSystemMetricHostGroupData();

  return (
    <>
      <div className="h-80">
        {isValidating ? (
          <ListItemSkeleton skeletonOption={{ viewBoxHeight: 320 }} />
        ) : (
          <VirtualList
            {...props}
            list={data}
            itemChild={(hostGroupName) => (
              <>
                <div className="truncate">{hostGroupName}</div>
              </>
            )}
          ></VirtualList>
        )}
      </div>
      <Button
        className="flex items-center w-full gap-1 font-semibold rounded-t-none"
        onClick={() => mutate()}
      >
        refetch <LuRotateCw />
      </Button>
    </>
  );
};
