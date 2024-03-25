export namespace InspectorAgentEvents {
  export interface Parameters {
    agentId: string;
    from: number;
    to: number;
    exclude: string;
  }

  export type Response = AgentEventData[];
  export interface AgentEventData {
    agentId: string;
    eventMessage?: string;
    eventTimestamp: number;
    eventTypeCode: number;
    eventTypeDesc: string;
    hasEventMessage: boolean;
    startTimestamp: number;
  }
}
