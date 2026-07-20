import { Transaction } from './Transaction';

export namespace TransactionTraceMetadata {
  export interface Parameters {
    traceId: string;
  }

  export interface Response {
    metadata: Transaction[];
    complete: boolean;
    resultFrom: number;
  }
}
