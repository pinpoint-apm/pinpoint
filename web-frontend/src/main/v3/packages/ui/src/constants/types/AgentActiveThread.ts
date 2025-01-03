/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace AgentActiveThread {
  export type AgentActiveThreadType = 'PING' | 'PONG' | 'REQUEST' | 'RESPONSE';

  export interface Request {
    type: AgentActiveThreadType;
    command?: string;
    parameters?: any;
  }

  export interface Response {
    type?: AgentActiveThreadType;
    result?: Result;
  }

  export interface Result {
    timeStamp: number;
    applicationName: string;
    activeThreadCounts: ActiveThreadCounts;
  }

  export interface ActiveThreadCounts {
    [key: string]: ActiveThreadStatus;
  }

  export interface ActiveThreadStatus {
    code: number;
    message: string;
    status?: number[];
  }
}
