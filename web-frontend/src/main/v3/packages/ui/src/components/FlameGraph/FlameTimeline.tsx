import React from 'react';
import { FlameAxis } from './FlameAxis';
import { FlameGraphConfigContext } from './FlameGraphConfigContext';

export interface FlameTimelineProps {
  width: number;
  start: number;
  end: number;
  zoom?: number;
}

export const FlameTimeline = ({ width, start, end, zoom = 1 }: FlameTimelineProps) => {
  const { config } = React.useContext(FlameGraphConfigContext);
  const { padding, yAxisCount } = config;
  const actualWidth = width - padding.left - padding.right;
  const timeGap = end - start;
  const actualYAxisCount = Math.ceil(yAxisCount * zoom);
  return (
    <>
      {Array.from(Array(actualYAxisCount)).map((_, i) => {
        return (
          <text
            key={i}
            x={(actualWidth / actualYAxisCount) * i + 14}
            y={12}
            fontSize={'0.625rem'}
            letterSpacing={-0.5}
            fill={config.color.time}
          >
            +{((timeGap / actualYAxisCount) * i).toFixed(1)}ms
          </text>
        );
      })}
      <FlameAxis width={width} zoom={zoom} />;
    </>
  );
};
