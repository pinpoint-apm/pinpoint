import { atom } from 'jotai';
import { Transaction, TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

export const transactionListDatasAtom = atom<
  | {
      complete?: boolean;
      resultFrom?: number;
      metadata: Transaction[];
    }
  | undefined
>(undefined);
export const transactionInfoDatasAtom = atom<TransactionInfo.Response | undefined>(undefined);
export const transactionInfoCurrentTabId = atom<string>('');
export const transactionInfoCallTreeFocusId = atom<string>('');
