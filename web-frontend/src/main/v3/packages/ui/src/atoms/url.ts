import { atom } from 'jotai';
import { UrlStatSummary } from '@pinpoint-fe/constants';

export const urlSelectedSummaryDataAtom = atom<UrlStatSummary.SummaryData>(
  {} as UrlStatSummary.SummaryData,
);
