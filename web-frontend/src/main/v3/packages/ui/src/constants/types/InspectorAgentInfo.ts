export namespace InspectorAgentInfoType {
  export interface Parameters {
    agentId: string;
    timestamp: number;
  }

  export interface Response {
    agentId: string;
    agentName: string;
    agentVersion: string;
    applicationName: string;
    hostName: string;
    initialStartTimestamp: number;
    ip: string;
    jvmInfo: {
      gcTypeName: string;
      jvmVersion: string;
      version: number;
    };
    pid: number;
    ports: string;
    serverMetaData: {
      serverInfo: string;
      serviceInfos: {
        serviceLibs: string[];
        serviceName: string;
      }[];
      vmArgs: string[];
    };
    serviceType: string;
    startTimestamp: number;
    status: {
      agentId: string;
      eventTimestamp: number;
      state: {
        code: number;
        desc: string;
      };
    };
    vmVersion: string;
  }
}
