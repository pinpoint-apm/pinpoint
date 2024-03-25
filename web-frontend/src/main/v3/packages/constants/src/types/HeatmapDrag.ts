export namespace HeatmapDrag {
  export interface Parameters {
    application: string;
    x1: number;
    x2: number;
    y1: number;
    y2: number;
    dotStatus?: boolean;
  }

  export interface Response {
    complete: boolean;
    resultFrom: number;
    metadata: Metadatum[];
  }

  export interface Metadatum {
    exception: number;
    agentId: string;
    spanId: string;
    elapsed: number;
    endpoint: { [key: string]: string };
    collectorAcceptTime: number;
    application: string;
    agentName: null;
    remoteAddr: { [key: string]: string };
    startTime: number;
    traceId: string;
  }
}
