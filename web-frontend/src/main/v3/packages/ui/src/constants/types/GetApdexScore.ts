export namespace GetApdexScore {
  export interface Parameters {
    applicationName: string;
    serviceTypeCode: number;
    from: number;
    to: number;
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
