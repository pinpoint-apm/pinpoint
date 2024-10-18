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
    fieldName: FieldName;
    chart: Chart;
    groupedFieldName: {
      errorClassName: string;
    };
    groupFilterParams: {
      errorClassName: string;
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

  export interface FieldName {
    stackTraceHash?: string;
    uriTemplate?: string;
    errorMessage?: string;
    errorClassName?: string;
  }
}
