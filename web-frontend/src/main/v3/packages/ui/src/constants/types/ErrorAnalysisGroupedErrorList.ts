import { Chart } from './common/Chart';

export namespace ErrorAnalysisGroupedErrorList {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    groupBy: string;
    agentId?: string;
  }

  export type Response = ErrorData[];
  export interface ErrorData {
    mostRecentErrorClass: string;
    mostRecentErrorMessage: string;
    count: number;
    firstOccurred: number;
    lastOccurred: number;
    chart: Chart;
    groupedFieldName: GroupedFieldName;
    groupFilterParams: {
      errorClassName?: string;
      errorMessage_logType?: string;
      errorStackTraceHash?: string;
    };
    firstLineOfClassName: string;
    firstLineOfMethodName: string;
    lastTransactionSearchParams: {
      applicationName: string;
      agentId: string;
      transactionId: string;
      spanId: string;
      exceptionId: string;
    };
  }

  export interface GroupedFieldName {
    stackTraceHash?: string;
    uriTemplate?: string;
    errorMessage?: string;
    errorClassName?: string;
  }
}
