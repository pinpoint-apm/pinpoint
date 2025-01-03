import React from 'react';
import { colors } from '@pinpoint-fe/ui/constants';

export const flameGraphDefaultConfig = {
  height: {
    node: 20,
    timeline: 48,
  },
  padding: {
    top: 8,
    bottom: 0,
    right: 10,
    left: 10,
    group: 20,
  },
  color: {
    axis: colors.gray[300],
    time: colors.gray[400],
  },
  yAxisCount: 10,
};

export const FlameGraphConfigContext = React.createContext<{
  config: typeof flameGraphDefaultConfig;
  // setConfig: React.Dispatch<React.SetStateAction<typeof flameGraphDefaultConfig>>;
}>({
  config: flameGraphDefaultConfig,
  // setConfig: () => {},
});
