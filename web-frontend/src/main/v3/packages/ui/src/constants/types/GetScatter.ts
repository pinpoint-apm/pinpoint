export namespace GetScatter {
  export interface Parameters {
    application?: string;
    from?: number;
    to?: number;
    limit?: number;
    filter?: string;
    xGroupUnit?: number;
    yGroupUnit?: number;
    backwardDirection?: boolean;
  }

  export interface Response {
    currentServerTime: number;
    from: number;
    to: number;
    scatter: Scatter;
    complete: boolean;
    resultFrom: number;
    resultTo: number;
  }

  export interface Scatter {
    metadata: Metadata;
    dotList: number[][];
  }

  export interface Metadata {
    [key: number]: [string, string, number];
  }
}
