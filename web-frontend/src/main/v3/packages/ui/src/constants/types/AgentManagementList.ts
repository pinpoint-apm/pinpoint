export namespace AgentManagementList {
  export type Response = Instance[];

  export interface Parameters {
    applicationName: string;
    serviceTypeName?: string;
    serviceTypeCode?: number;
  }

  export interface State {
    code: number;
    desc: string;
  }

  export interface Instance {
    agentId: string;
    agentStartTime: number;
    agentName: string;
    // state within the query range
    state: State;
    // most recently persisted state of the agent
    lastState: State;
    lastStateUpdateTimestamp: number;
    applicationName: string;
    serviceTypeCode: number;
    serviceTypeName: string;
  }
}
