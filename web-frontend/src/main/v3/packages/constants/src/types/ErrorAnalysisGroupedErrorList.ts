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
  }

  export interface FieldName {
    stackTraceHash?: string;
    uriTemplate?: string;
    errorMessage?: string;
    errorClassName?: string;
  }
}
