export namespace ErrorAnalysisErrorList {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    orderBy: string;
    isDesc: boolean;
    count: number;
    agentId?: string;
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
    stackTrace: StackTrace[]; // length: 3
    stackTraceHash: string;
  }

  export interface StackTrace {
    className: string;
    fileName: string;
    lineNumber: number;
    methodName: string;
  }
}
