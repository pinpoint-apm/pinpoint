import { cn } from '../../../lib';
import {
  InspectorAgentStatusTimelineType as InspectorAgentStatusTimeline,
  colors,
} from '@pinpoint-fe/ui/constants';
import { TimelineBar, TimelineBarProps } from './TimelineBar';
import { TimelineEvent } from './TimelineEvent';

export const AGENT_STATUS_COLOR = {
  BASE: colors.gray[300],
  UNKNOWN: colors.neutral[300],
  RUNNING: colors.emerald[400],
  SHUTDOWN: colors.red[400],
  UNSTABLE_RUNNING: colors.orange[400],
  EMPTY: colors.blue[300],
} as const;

export interface StatusTimelineProps
  extends Pick<
    TimelineBarProps,
    'totalRange' | 'activeRange' | 'hideTick' | 'tickCount' | 'formatTick' | 'formatTooltip'
  > {
  data: InspectorAgentStatusTimeline.Response;
  wrapperClassName?: string;
  barClassName?: string;
}

export const StatusTimeline = ({
  data,
  wrapperClassName,
  barClassName,
  totalRange,
  ...props
}: StatusTimelineProps) => {
  const eventSegments = data.agentEventTimeline.timelineSegments;
  const timelineSegments = data.agentStatusTimeline.timelineSegments;
  const rangeDiff = totalRange[1] - totalRange[0];
  const gradient = getGradient(timelineSegments, rangeDiff).join(',');
  const background = `linear-gradient(to right, ${gradient})`;

  return (
    <div className={cn('relative', wrapperClassName)}>
      <TimelineBar
        className={barClassName}
        background={background}
        totalRange={totalRange}
        {...props}
      />
      {eventSegments.length !== 0 && <TimelineEvent data={eventSegments} range={totalRange} />}
    </div>
  );
};

const getGradient = (
  data: {
    startTimestamp: number;
    endTimestamp: number;
    value: string;
  }[] = [],
  totalWeight: number,
) => {
  let gradientStart = 0;
  let gradientEnd = 0;

  return data.reduce((acc, { startTimestamp, endTimestamp, value }) => {
    const color = AGENT_STATUS_COLOR[value as keyof typeof AGENT_STATUS_COLOR];
    const weight = ((endTimestamp - startTimestamp) / totalWeight) * 100;
    gradientStart = gradientEnd;
    gradientEnd = gradientStart + weight;

    return [...acc, `${color} ${gradientStart}%`, `${color} ${gradientEnd}%`];
  }, [] as string[]);
};
