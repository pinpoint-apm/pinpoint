import React from 'react';
import { FlameGraphConfigContext } from './FlameGraphConfigContext';

export interface FlameAxisProps {
  width: number;
}

export const FlameAxis = ({ width }: FlameAxisProps) => {
  const { config } = React.useContext(FlameGraphConfigContext);
  const { padding, color, yAxisCount } = config;
  const actualWidth = width - padding.left - padding.right;

  return (
    <>
      {Array.from(Array(yAxisCount)).map((_, i) => {
        return (
          <line
            key={i}
            x1={(actualWidth / yAxisCount) * i + padding.left}
            y1={0}
            x2={(actualWidth / yAxisCount) * i + padding.left}
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
