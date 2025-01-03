import { atom } from 'jotai';

type UserMetricConfig = {
  title?: string;
  unit?: string;
  chartType?: string;
};

export const userMetricConfigAtom = atom<UserMetricConfig>({});
