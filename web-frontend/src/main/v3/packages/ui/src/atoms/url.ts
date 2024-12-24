import { atom } from 'jotai';
import { UrlStatSummary } from '@pinpoint-fe/ui/constants';

export const urlSelectedSummaryDataAtom = atom<UrlStatSummary.SummaryData>(
  {} as UrlStatSummary.SummaryData,
);
