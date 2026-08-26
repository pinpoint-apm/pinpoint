export namespace ErrorAnalysisTransactionInfo {
  export interface Parameters {
    // TODO #14196: make required (missing values default to DEFAULT)
    serviceName?: string;
    applicationName: string;
    agentId: string;
    transactionId: string;
    spanId: string;
    exceptionId: string;
  }

  export type Response = ErrorData[];
  export interface ErrorData {
    timestamp: number;
    transactionId: string;
    spanId: string;
    exceptionId: string;
    applicationServiceType: string;
    applicationName: string;
    agentId: string;
    uriTemplate: string;
    errorClassName: string;
    errorMessage: string;
    exceptionDepth: number;
    stackTrace: StackTrace[];
    stackTraceHash: string;
  }

  export interface StackTrace {
    className: string;
    fileName: string;
    lineNumber: number;
    methodName: string;
  }
}
