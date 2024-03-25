export namespace InspectorAgentChart {
  export interface Parameters {
    agentId: string;
    from: number;
    to: number;
    metricDefinitionId: string;
  }

  export interface Response {
    title: string;
    timestamp: number[];
    metricValues: MetricValue[];
  }

  export interface MetricValue {
    chartType: string;
    fieldName: string;
    unit: string;
    valueList: number[];
    tagList?: unknown[];
  }
}
