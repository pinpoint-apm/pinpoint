export namespace SqlStatSummary {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    // col?: string;
    query?: string;
    orderBy?: string;
    groupBy?: string;
    isDesc?: boolean;
    count?: number;
  }

  export type Response = SummaryData[];
  export interface SummaryData {
    id: string; // for BE only
    totalCount: number;
    avgTime: number;
    totalTime: number;
    label: string;
  }
}
