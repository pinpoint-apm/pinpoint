export namespace SearchApplication {
  export type Response = Application[];

  export interface Parameters {
    application: string;
    serviceTypeName?: string;
    serviceTypeCode?: number;
    applicationPairs?: string;
    from?: number;
    to?: number;
    sortBy?: string;
  }

  export interface Application {
    groupName: string;
    instancesList: Instance[];
  }

  export interface Instance {
    applicationName: string;
    agentId: string;
    agentName: string;
    startTimestamp: number;
    hostName: string;
    ip: string;
    ports: string;
    serviceType: string;
    pid: number;
    vmVersion: string;
    agentVersion: string;
    container: boolean;
    serviceTypeCode: number;
    status: Status;
    linkList: LinkList[];
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
