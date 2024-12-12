import { Button } from '../ui';
import { useGetApplicationList } from '@pinpoint-fe/ui/hooks';
import { LuRotateCw } from 'react-icons/lu';

import { ApplicationVirtualListProps, ApplicationVirtualList } from '.';
import { ListItemSkeleton } from '../VirtualList';

export const ApplicationListFetcher = (props: ApplicationVirtualListProps) => {
  const { data, mutate, isValidating } = useGetApplicationList();

  return (
    <>
      <div className="h-80">
        {isValidating ? (
          <ListItemSkeleton skeletonOption={{ viewBoxHeight: 320 }} />
        ) : (
          <ApplicationVirtualList {...props} list={data} />
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
