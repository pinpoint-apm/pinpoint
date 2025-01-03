export namespace TraceViewerData {
  export interface Parameters {
    traceId?: string;
    focusTimestamp?: string;
    agentId?: string;
    spanId?: string;
  }

  export interface Response {
    traceEvents: TraceEvent[];
  }

  export interface TraceEvent {
    cat: string;
    tid: number;
    id: string;
    ts: number;
    ph: string;
    dur: number;
    s: string;
    name: string;
    cname: string;
    args: Args;
    pid: string;
  }

  export interface Args {
    id: string;
    'API Type': string;
    'Application Name': string;
    parentId: string;
    Query?: string;
  }
}
