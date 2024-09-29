import { Transaction } from './Transaction';

export namespace TransactionMetaData {
  export interface PostParameters {
    ApplicationName: string;
    [key: string]: string;
  }

  export interface PostResponse {
    metadata: Transaction[];
  }
}
