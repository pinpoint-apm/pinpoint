import { Badge, Popover, PopoverContent, PopoverTrigger } from '../../ui';
import { LuArrowUp } from 'react-icons/lu';
import { InspectorAgentEventViewer } from '.';

export interface TimelineEventProps {
  data: {
    startTimestamp: number;
    endTimestamp: number;
    value: {
      totalCount: number;
    };
  }[];
  range: number[];
}

export const TimelineEvent = ({ data, range: [from, to] }: TimelineEventProps) => {
  const rangeDiff = to - from;

  return (
    <div className="relative">
      {data.map(({ startTimestamp, endTimestamp, value }, i) => {
        const pos = (((startTimestamp + endTimestamp) / 2 - from) / rangeDiff) * 100;
        return (
          <div
            key={i}
            style={{ left: `${pos}%` }}
            className="absolute text-center -translate-x-1/2 -top-1"
          >
            <LuArrowUp />
            <Popover>
              <PopoverTrigger asChild>
                <div>
                  <Badge
                    className="flex items-center py-1 font-normal cursor-pointer text-xxs hover:bg-accent hover:text-accent-foreground"
                    variant="outline"
                  >
                    <span className="lg:hidden">{value.totalCount}</span>
                    <span className="hidden lg:inline">{value.totalCount} Events</span>
                  </Badge>
                </div>
              </PopoverTrigger>
              <PopoverContent className="px-1 py-0">
                <InspectorAgentEventViewer eventRange={[startTimestamp, endTimestamp]} />
              </PopoverContent>
            </Popover>
          </div>
        );
      })}
    </div>
  );
};
