export namespace AgentList {
  export type Response = Instance[];

  type BaseParameters = {
    applicationName: string;
    from?: number | string;
    to?: number | string;
  };

  export type Parameters = BaseParameters &
    (
      | { serviceTypeName: string; serviceTypeCode?: number }
      | { serviceTypeName?: string; serviceTypeCode: number }
    );

  export interface Instance {
    applicationName: string;
    serviceTypeName: string;
    serviceTypeCode: number;
    agentId: string;
    agentStartTime: number;
    agentName: string;
    state: LifeCycleState;
    currentState: LifeCycleState;
    currentStateTimestamp: number;
  }

  export interface LifeCycleState {
    code: number;
    desc: string;
  }
}
