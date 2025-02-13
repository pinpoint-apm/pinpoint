import React from 'react';
import { format } from 'date-fns';
import { cn } from '../../../lib';
import { colors } from '@pinpoint-fe/ui/src/constants';
import { InteractiveTimelineBar, InteractiveTimelineBarProps } from './InteractiveTimelineBar';

export interface TimelineBarProps
  extends Pick<InteractiveTimelineBarProps, 'activeRange' | 'formatTooltip'> {
  totalRange: [number, number];
  className?: string;
  background?: string;
  hideTick?: boolean;
  tickCount?: number;
  formatTick?: (value: number) => React.ReactNode;
}

export const TimelineBar = ({
  totalRange: [totalFrom, totalTo],
  className,
  background = colors.gray[300],
  hideTick = false,
  tickCount = 5,
  formatTick = (v) => format(v, 'MM.dd HH:mm'),
  ...props
}: TimelineBarProps) => {
  const rangeDiff = totalTo - totalFrom;
  const ticks = Array.from(Array(tickCount + 2)).map((_, i) => {
    return (rangeDiff / (tickCount + 1)) * i + totalFrom;
  });

  return (
    <div className={cn('relative, w-full', className)}>
      <div
        style={{
          background,
          opacity: 0.7,
        }}
        className="absolute top-0 left-0 w-full h-full rounded-sm"
      />
      <InteractiveTimelineBar totalRange={[totalFrom, totalTo]} {...props} />
      {!hideTick && (
        <>
          <div className="relative z-10 flex h-full justify-evenly">
            {Array.from(Array(tickCount)).map((_, i) => {
              return <div key={i} className="w-0.5 h-full bg-white"></div>;
            })}
          </div>
          <div>
            {ticks.map((tick, i) => {
              return (
                <div
                  key={i}
                  style={{ left: `${(100 / (tickCount + 1)) * i}%` }}
                  className="absolute -translate-x-1/2 -top-5 text-nowrap"
                >
                  {formatTick(tick)}
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
};
