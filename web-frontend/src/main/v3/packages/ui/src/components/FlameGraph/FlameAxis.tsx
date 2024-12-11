import React from 'react';
import { FlameGraphConfigContext } from './FlameGraphConfigContext';

export interface FlameAxisProps {
  width: number;
  zoom?: number;
}

export const FlameAxis = ({ width, zoom = 1 }: FlameAxisProps) => {
  const { config } = React.useContext(FlameGraphConfigContext);
  const { padding, color, yAxisCount } = config;
  const actualWidth = width - padding.left - padding.right;
  const actualYAxisCount = Math.ceil(yAxisCount * zoom);

  return (
    <>
      {Array.from(Array(actualYAxisCount)).map((_, i) => {
        return (
          <line
            key={i}
            x1={(actualWidth / actualYAxisCount) * i + padding.left}
            y1={0}
            x2={(actualWidth / actualYAxisCount) * i + padding.left}
            y2="100%"
            stroke={color.axis}
          />
        );
      })}
      <line
        x1={width - padding.right}
        y1={0}
        x2={width - padding.right}
        y2="100%"
        stroke={color.axis}
      />
    </>
  );
};
