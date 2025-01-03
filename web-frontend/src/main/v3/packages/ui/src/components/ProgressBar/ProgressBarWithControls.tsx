import React from 'react';
import { format } from 'date-fns';
import { FaCheck, FaPause } from 'react-icons/fa';
import { FaArrowRotateRight } from 'react-icons/fa6';
import { useUpdateEffect } from 'usehooks-ts';
import { ProgressBar, ProgressBarProps } from './ProgressBar';
import { CgSpinner } from 'react-icons/cg';
import { cn } from '../../lib';
import { colors } from '@pinpoint-fe/ui/constants';
import { LuRotateCcw } from 'react-icons/lu';

export interface ProgressBarWithControlsProps extends ProgressBarProps {
  children?: (props: {
    isComplete: boolean;
    isPause: boolean;
    completeRenderer: React.ReactNode;
    resumeRenderer: React.ReactNode;
  }) => React.ReactNode;
  onClickPause?: () => void;
  onClickResume?: () => void;
}

export const ProgressBarWithControls = ({
  className,
  onClickPause,
  onClickResume,
  onChange,
  children,
  ...props
}: ProgressBarWithControlsProps) => {
  const [isPause, setPause] = React.useState(false);
  const [isComplete, setComplete] = React.useState(
    (props?.progress || 0) >= (props?.range?.[0] || 0),
  );

  useUpdateEffect(() => {
    setPause(false);
    setComplete(false);
  }, [props.range?.[0], props.range?.[1]]);

  const handleClickPause = () => {
    setPause(true);
    onClickPause?.();
  };

  const handleClickResume = () => {
    setPause(false);
    onClickResume?.();
  };

  const completeRenderer = <FaCheck className="w-8 h-8" fill={colors['status-success']} />;
  const resumeRenderer = (
    <LuRotateCcw className="w-8 h-8 cursor-pointer" onClick={handleClickResume} />
  );

  return (
    <div className={cn('flex gap-8 py-3 px-7 pr-4 h-12 rounded bg-white', className)}>
      <ProgressBar
        reverse
        className="w-full text-xxs"
        colors={[colors.emerald[200], colors['status-success']]}
        formatTick={(value) => {
          return format(value, 'HH:mm');
        }}
        onChange={({ percent }) => {
          onChange?.({ percent });
          if (percent === 100) {
            setComplete(true);
          } else {
            setComplete(false);
          }
        }}
        {...props}
      />
      <div className="relative w-8 min-w-[2rem] flex items-center justify-center">
        {typeof children === 'function' ? (
          children({ isComplete, isPause, completeRenderer, resumeRenderer })
        ) : (
          <>
            {isComplete ? (
              completeRenderer
            ) : isPause ? (
              <FaArrowRotateRight className="w-8 h-8" onClick={handleClickResume} />
            ) : (
              <>
                <CgSpinner
                  className="absolute w-10 h-10 cursor-pointer animate-spin"
                  onClick={handleClickPause}
                />
                <FaPause className="w-3 h-3" />
              </>
            )}
          </>
        )}
      </div>
    </div>
  );
};
