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
    <div className={cn('relative', className)}>
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

// const StyledProgressBarContainer = styled.div`
//   position: relative;
//   width: 100%;
//   height: 10px;
//   border-radius: 3px;
//   display: flex;
//   justify-content: space-evenly;
//   background-color: var(--border-primary-lighter);
// `;

// const StyledProgressBar = styled.div<{
//   reverse: boolean;
//   colors: ProgressBarProps['colors'];
// }>`
//   position: absolute;
//   top: 0px;
//   height: 100%;
//   border-radius: 3px;

//   transition: 0.75s ease;
//   transition-delay: 0.1s;
//   ${({ reverse, colors }) => {
//     if (reverse) {
//       return {
//         right: 0,
//         background: `linear-gradient(90deg, ${colors![1]} 0%, ${colors![0]} 100%)`,
//       };
//     }
//     return {
//       left: 0,
//       background: `linear-gradient(90deg, ${colors![0]} 0%, ${colors![1]} 100%)`,
//     };
//   }}
// `;

// const StyledTickBar = styled.div`
//   width: 2px;
//   height: 100%;
//   background: var(--white-default);
//   z-index: 1;
// `;

// const StyledTickContainer = styled.div`
//   display: flex;
// `;

// const StyledTickWrapper = styled.div<{
//   index: number;
//   tickCount: number;
//   reverse: boolean;
// }>`
//   position: absolute;
//   transform: translateX(-50%);

//   ${({ reverse, index, tickCount }) => {
//     if (reverse) {
//       return {
//         right: `${(100 / (tickCount + 1)) * index}%`,
//         transform: 'translateX(50%)',
//       };
//     } else {
//       return {
//         left: `${(100 / (tickCount + 1)) * index}%`,
//         transform: 'translateX(-50%)',
//       };
//     }
//   }}
// `;
