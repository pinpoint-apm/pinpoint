import { useGetInspectorAgentStatusTimeline } from '@pinpoint-fe/ui/src/hooks';
import { cn } from '../../../../lib';
import { StatusTimeline } from '../StatusTimeline';

export interface InspectorApplicationStatusTimelineFetcherProps {
  className?: string;
}

export const InspectorApplicationStatusTimelineFetcher = ({
  className,
}: InspectorApplicationStatusTimelineFetcherProps) => {
  const { totalRange, activeRange } = useGetInspectorAgentStatusTimeline();
  const data = {
    agentStatusTimeline: {
      timelineSegments: [
        {
          startTimestamp: totalRange[0],
          endTimestamp: totalRange[1],
          value: 'EMPTY',
        },
      ],
      includeWarning: false,
    },
    agentEventTimeline: {
      timelineSegments: [],
    },
  };

  return (
    <div className={cn('py-4 px-4 h-24', className)}>
      <StatusTimeline
        data={data}
        totalRange={totalRange}
        activeRange={activeRange}
        wrapperClassName="w-full text-xxs"
        barClassName="h-10"
      />
    </div>
  );
};
