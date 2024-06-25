import React from 'react';
import { useUpdateEffect } from 'usehooks-ts';
import { cn } from '../../lib';

export interface ProgressBarProps {
  className?: string;
  range?: [number, number];
  colors?: [string, string];
  progress?: number;
  tickCount?: number;
  reverse?: boolean;
  hideTick?: boolean;
  style?: React.CSSProperties;
  formatTick?: (value: number) => React.ReactNode;
  onChange?: ({ percent }: { percent: number }) => void;
}

export const ProgressBar = ({
  className,
  range = [0, 100],
  colors = ['var(--blue-300)', 'var(--blue-600)'],
  progress = range[0],
  tickCount = 5,
  reverse = false,
  hideTick = false,
  style,
  formatTick,
  onChange,
}: ProgressBarProps) => {
  const CLASS_PREFIX = '__pinpoint_progress_bar__';
  const BAR_CLASS = `${CLASS_PREFIX}bar__`;
  const BAR_CONTAINER_CLASS = `${CLASS_PREFIX}bar_conatainer__`;
  const TICK_CLASS = `${CLASS_PREFIX}tick_wrapper__`;
  const rangeDiff = range[1] - range[0];
  const [percent, setPercent] = React.useState(((progress - range[0]) / rangeDiff) * 100);

  const ticks = Array.from(Array(tickCount + 2)).map((_, i) => {
    return (rangeDiff / (tickCount + 1)) * i + range[0];
  });

  useUpdateEffect(() => {
    let calculatedPercent = ((progress - range[0]) / rangeDiff) * 100;

    if (calculatedPercent < 0) {
      calculatedPercent = 0;
    } else if (calculatedPercent > 100) {
      calculatedPercent = 100;
    }
    setPercent(calculatedPercent);

    onChange?.({ percent: calculatedPercent });
  }, [progress]);

  return (
    <div className={cn('relative', className)} style={style}>
      <div
        className={cn(
          'relative, w-full h-2.5 rounded-sm flex justify-evenly bg-border',
          BAR_CONTAINER_CLASS,
        )}
      >
        <div
          style={{
            width: `${percent}%`,
            background: reverse
              ? `linear-gradient(90deg, ${colors![1]} 0%, ${colors![0]} 100%)`
              : `linear-gradient(90deg, ${colors![0]} 0%, ${colors![1]} 100%)`,
          }}
          className={cn(
            'absolute top-0 h-2.5 rounded-sm transition-all delay-100 duration-700',
            {
              'left-0': !reverse,
            },
            {
              'right-0': reverse,
            },
            BAR_CLASS,
          )}
        />
        {!hideTick &&
          Array.from(Array(tickCount)).map((_, i) => {
            return <div key={i} className="w-0.5 h-full bg-white z-10"></div>;
          })}
      </div>
      {!hideTick && (
        <div className="flex">
          {ticks.map((tick, i) => {
            return (
              <div
                key={i}
                style={{ [reverse ? 'right' : 'left']: `${(100 / (tickCount + 1)) * i}%` }}
                className={cn(
                  `absolute top-3 -translate-x-1/2`,
                  { 'translate-x-1/2': reverse },
                  TICK_CLASS,
                )}
              >
                {formatTick ? formatTick?.(tick) : tick}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
