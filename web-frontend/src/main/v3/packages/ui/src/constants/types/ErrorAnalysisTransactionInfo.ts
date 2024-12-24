export namespace ErrorAnalysisTransactionInfo {
  export interface Parameters {
    applicationName: string;
    agentId: string;
    transactionId: string;
    spanId: number;
    exceptionId: number;
  }

  export type Response = ErrorData[];
  export interface ErrorData {
    timestamp: number;
    transactionId: string;
    spanId: number;
    exceptionId: number;
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
