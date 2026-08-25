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
export const transactionInfoDatasAtom = atom<TransactionInfo.Response | null>(null);
export const transactionInfoCurrentTabId = atom<string>('');
export const transactionInfoCallTreeFocusId = atom<string>('');
// "Step in": the Call Tree and the Flame Graph are re-rooted at this call stack id so that only
// that span and its descendants are drawn. Shared by both tabs because the trace viewer's
// `args.id` is the very same call stack record id as the Call Tree row's `id` (the backend puts
// `record.getId()` in both). Empty string means "stepped out" — showing the whole trace.
export const transactionInfoSteppedInSpanId = atom<string>('');
