import { useGetInspectorAgentStatusTimeline } from '@pinpoint-fe/ui/src/hooks';
import { cn } from '../../../../lib';
import { StatusTimeline } from '../StatusTimeline';

export interface InspectorAgentStatusTimelineFetcherProps {
  className?: string;
}

export const InspectorAgentStatusTimelineFetcher = ({
  className,
}: InspectorAgentStatusTimelineFetcherProps) => {
  const { data, totalRange, activeRange } = useGetInspectorAgentStatusTimeline();

  return (
    <div className={cn('py-4 px-4 h-24', className)}>
      {data && (
        <StatusTimeline
          data={data}
          totalRange={totalRange}
          activeRange={activeRange}
          wrapperClassName="w-full text-xxs"
          barClassName="h-10"
        />
      )}
    </div>
  );
};
