export namespace ActiveThreadDump {
  export interface Parameters {
    applicationName: string;
    agentId: string;
    threadName: string;
    localTraceId: number;
  }

  export interface Response {
    code: number;
    message: Message;
  }

  export interface Message {
    threadDumpData: ThreadDumpData[];
    type: string;
    subType: string;
    version: string;
  }

  export interface ThreadDumpData {
    threadId: string;
    threadName: string;
    threadState: string;
    startTime: number;
    execTime: number;
    localTraceId: number;
    sampled: boolean;
    transactionId: string;
    entryPoint: string;
    detailMessage: string;
  }

  export interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    exception: string;
    trace: string;
    message: string;
    path: string;
    data: ErrorData;
  }

  export interface ErrorData {
    hostName: string;
    requestInfo: RequestInfo;
  }

  export interface RequestInfo {
    method: string;
    url: string;
    headers: Headers;
    parameters: Parameters;
  }

  export interface Headers {
    'sec-fetch-mode': string[];
    referer: string[];
    'sec-fetch-site': string[];
    'accept-language': string[];
    cookie: string[];
    pragma: string[];
    accept: string[];
    'sec-ch-ua': string[];
    'sec-ch-ua-mobile': string[];
    'sec-ch-ua-platform': string[];
    'cache-control': string[];
    'accept-encoding': string[];
    'user-agent': string[];
    'sec-fetch-dest': string[];
  }
}
