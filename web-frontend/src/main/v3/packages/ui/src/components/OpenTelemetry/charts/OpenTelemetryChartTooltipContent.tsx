import React from 'react';
import { format } from 'date-fns';
import { ChartTooltipContent } from '../../ui/chart';
import { cn } from '../../../lib';
import { COLORS } from './constant';

const TOTAL_DATA_KEY = 'total' as const;
const TOTAL_DATA_COLOR = COLORS[COLORS.length - 1];
type CustomChartTooltipContentProps = Omit<
  React.ComponentProps<typeof ChartTooltipContent>,
  'formatter'
> & {
  formatter?: (value: number, index: number) => string;
  showTotal?: boolean;
};
export interface OpenTelemetryChartTooltipContentProps extends CustomChartTooltipContentProps {}

export const OpenTelemetryChartTooltipContent = ({
  active,
  payload = [],
  hideLabel,
  label,
  labelClassName,
  indicator = 'dot',
  labelFormatter = (label) => format(label, 'HH:mm:ss'),
  formatter = (value) => `${value}`,
  showTotal = false,
}: OpenTelemetryChartTooltipContentProps) => {
  const renderTooltipLabel = () => {
    if (hideLabel || !payload?.length) {
      return null;
    }

    return (
      <div className={cn('font-medium', labelClassName)}>{labelFormatter(label, payload)}</div>
    );
  };

  const tooltipPayload = showTotal
    ? [
        ...payload,
        {
          dataKey: TOTAL_DATA_KEY,
          name: TOTAL_DATA_KEY,
          value: payload?.reduce((acc, curr) => {
            const value = curr.value as number;

            return acc + value;
          }, 0),
          color: TOTAL_DATA_COLOR,
        },
      ]
    : payload;

  return (
    active && (
      <div className="grid min-w-[8rem] items-start gap-1.5 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl w-max">
        {renderTooltipLabel()}
        <div className="grid gap-1.5">
          {tooltipPayload?.map((item, index) => {
            const indicatorColor = item.color;
            // const nestLabel = payload.length === 1 && indicator !== 'dot';

            return (
              <div
                key={item.dataKey}
                className={cn(
                  'flex w-full flex-wrap items-stretch gap-2 [&>svg]:h-2.5 [&>svg]:w-2.5 [&>svg]:text-muted-foreground',
                  indicator === 'dot' && 'items-center',
                )}
              >
                <>
                  <div
                    className={cn(
                      'shrink-0 rounded-[2px] border-[--color-border] bg-[--color-bg]',
                      {
                        'h-2.5 w-2.5': indicator === 'dot',
                        'w-1': indicator === 'line',
                        'w-0 border-[1.5px] border-dashed bg-transparent': indicator === 'dashed',
                        'my-0.5': indicator === 'dashed',
                      },
                    )}
                    style={
                      {
                        '--color-bg': indicatorColor,
                        '--color-border': indicatorColor,
                      } as React.CSSProperties
                    }
                  />
                  <div
                    className={cn(
                      'flex flex-1 justify-between leading-none',
                      // nestLabel ? 'items-end' : 'items-center',
                    )}
                  >
                    <div className="grid gap-1.5">
                      <span className="text-muted-foreground">{item.name}</span>
                    </div>
                    {item.value && (
                      <span className="font-mono font-medium tabular-nums text-foreground">
                        {formatter(item.value as number, index)}
                      </span>
                    )}
                  </div>
                </>
              </div>
            );
          })}
        </div>
      </div>
    )
  );
};
