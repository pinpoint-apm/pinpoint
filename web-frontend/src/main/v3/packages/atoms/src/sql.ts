import { atom } from 'jotai';
import { SqlStatSummary } from '@pinpoint-fe/constants';

export const sqlSelectedSummaryDatasAtom = atom<SqlStatSummary.SummaryData[]>([]);
