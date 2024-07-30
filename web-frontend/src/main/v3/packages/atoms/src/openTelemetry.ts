import { atom } from 'jotai';

type UserMetricConfig = {
  chartType?: string;
  yAxisUnit?: string;
  metricTitle?: string;
};

export const openMetricDefinitionAtom = atom<boolean>(true);
export const userMetricConfigAtom = atom<UserMetricConfig>({});
