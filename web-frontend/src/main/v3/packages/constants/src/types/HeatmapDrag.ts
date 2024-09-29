import { Transaction } from './Transaction';

export namespace HeatmapDrag {
  export interface Parameters {
    application: string;
    x1: number;
    x2: number;
    y1: number;
    y2: number;
    dotStatus?: boolean;
  }

  export interface Response {
    complete: boolean;
    resultFrom: number;
    metadata: Transaction[];
  }
}
