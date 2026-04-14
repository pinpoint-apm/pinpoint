export namespace GetApdexScore {
  export interface Parameters {
    applicationName: string;
    serviceTypeName?: string;
    serviceTypeCode?: number;
    from: number | string;
    to: number | string;
    agentId?: string;
  }

  export interface Response {
    apdexScore: number;
    apdexFormula: ApdexFormula;
  }

  export interface ApdexFormula {
    satisfiedCount: number;
    toleratingCount: number;
    totalSamples: number;
  }
}
