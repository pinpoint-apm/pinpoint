export namespace AgentOverview {
  export type Response = Instance[];

  export interface Parameters {
    application: string;
    serviceTypeName?: string;
    serviceTypeCode?: number;
    applicationPairs?: string;
    from?: number;
    to?: number;
  }

  export interface Instance {
    agentId: string;
    agentName: string;
    agentVersion: string;
    applicationName: string;
    container: boolean;
    hasInspector: boolean;
    hostName: string;
    ip: string;
    linkList: LinkList[];
    pid: number;
    ports: string;
    serviceType: string;
    serviceTypeCode: number;
    startTimestamp: number;
    status: Status;
    vmVersion: string;
  }

  export interface Status {
    agentId: string;
    eventTimestamp: number;
    state: State;
  }

  export interface State {
    code: number;
    desc: string;
  }

  export interface LinkList {
    linkName: string;
    linkURL: string;
    linkType: string;
  }
}
