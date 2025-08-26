import { Button } from '../ui';
import { useGetApplicationList } from '@pinpoint-fe/ui/src/hooks';
import { LuRotateCw } from 'react-icons/lu';

import { ApplicationVirtualListProps, ApplicationVirtualList } from '.';
import { ListItemSkeleton } from '../VirtualList';

export const ApplicationListFetcher = (props: ApplicationVirtualListProps) => {
  const { data, refetch, isFetching } = useGetApplicationList();

  return (
    <>
      <div className="h-80">
        {isFetching ? (
          <ListItemSkeleton skeletonOption={{ viewBoxHeight: 320 }} />
        ) : (
          <ApplicationVirtualList {...props} list={data} />
        )}
      </div>
      <Button
        className="flex items-center w-full gap-1 font-semibold rounded-t-none"
        onClick={() => refetch()}
      >
        refetch <LuRotateCw />
      </Button>
    </>
  );
};
