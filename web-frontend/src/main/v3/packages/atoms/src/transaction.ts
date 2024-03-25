import { atom } from 'jotai';
import { HeatmapDrag, TransactionInfo } from '@pinpoint-fe/constants';

export const transactionListDatasAtom = atom<HeatmapDrag.Response | undefined>(undefined);
export const transactionInfoDatasAtom = atom<TransactionInfo.Response | undefined>(undefined);
