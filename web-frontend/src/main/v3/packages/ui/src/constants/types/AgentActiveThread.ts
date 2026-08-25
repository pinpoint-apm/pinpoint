export namespace AgentActiveThread {
  export type AgentActiveThreadType = 'PING' | 'PONG' | 'REQUEST' | 'RESPONSE';

  export interface Request {
    type: AgentActiveThreadType;
    command?: string;
    parameters?: RequestParameters;
  }

  /**
   * activeThreadCount 요청 파라미터.
   *
   * 소켓은 요청 헤더가 없으므로 service 정보를 메시지에 실어 보낸다. 세 값은 한 벌로 움직여야
   * 한다 - applicationName만 그대로 두고 serviceName/serviceType이 다른 노드의 값으로 바뀌면
   * 그 service에 없는 application을 묻는 요청이 된다.
   */
  export interface RequestParameters {
    applicationName: string;
    serviceType?: string;
    /** enableServiceMap이 꺼져 있으면 undefined - JSON 직렬화에서 빠지므로 예전 형태로 나간다. */
    serviceName?: string;
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
    agentName: string;
    code: number;
    message: string;
    status?: number[];
  }
}
