import React from 'react';
import { formatInTimeZone } from 'date-fns-tz';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../../ui';
import { useTimezone } from '@pinpoint-fe/ui/src/hooks';

export interface InteractiveTimelineBarProps {
  totalRange: [number, number];
  activeRange: [number, number];
  formatTooltip?: (value: number) => React.ReactNode;
}

export const InteractiveTimelineBar = ({
  totalRange: [totalFrom, totalTo],
  activeRange,
  formatTooltip,
}: InteractiveTimelineBarProps) => {
  const [timezone] = useTimezone();
  const rangeDiff = totalTo - totalFrom;
  const [activeFromInRatio, activeToInRatio] = activeRange.map((time) => {
    return ((time - totalFrom) / rangeDiff) * 100;
  });

  return (
    <TooltipProvider>
      <div className="absolute top-0 left-0 w-full h-full bg-transparent rounded-sm">
        <div
          style={{
            left: `${activeFromInRatio}%`,
            width: `${activeToInRatio - activeFromInRatio}%`,
            opacity: 0.4,
          }}
          className="absolute top-0 h-full bg-emerald-700"
        />
        {[activeFromInRatio, activeToInRatio].map((value, i) => (
          <div
            key={i}
            style={{
              left: `${value}%`,
            }}
            className="absolute z-20 w-0.5 h-full bg-gray-500"
          >
            <Tooltip open={true}>
              <TooltipTrigger asChild>
                <div className="absolute w-4 h-4 -translate-x-1/2 -translate-y-1/2 bg-white border-2 border-gray-500 rounded-md left-1/2 top-1/2 hover:bg-accent" />
              </TooltipTrigger>
              <TooltipContent side={'right'} className="px-0.5 text-xs text-black bg-transparent">
                {formatTooltip
                  ? formatTooltip(activeRange[i])
                  : formatInTimeZone(activeRange[i], timezone, 'MM.dd HH:mm')}
              </TooltipContent>
            </Tooltip>
          </div>
        ))}
      </div>
    </TooltipProvider>
  );
};
