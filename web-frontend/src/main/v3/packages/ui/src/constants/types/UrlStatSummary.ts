import { Chart } from './common/Chart';
export namespace UrlStatSummary {
  export interface Parameters {
    applicationName: string;
    from: number | string;
    to: number | string;
    orderBy: string;
    isDesc: boolean;
    count: number;
    agentId?: string;
    type?: 'total' | 'failure' | 'apdex' | 'latency';
  }

  export type Response = SummaryData[];
  export interface SummaryData {
    uri: string;
    totalCount: number;
    failureCount: number;
    apdex: number;
    avgTimeMs: number;
    maxTimeMs: number;
    version: string;
    chart: Chart;
  }
}
