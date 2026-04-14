export namespace InspectorAgentDataSourceChart {
  export interface Parameters {
    agentId: string;
    from: number | string;
    to: number | string;
    metricDefinitionId: string;
  }

  export interface Response {
    title: string;
    timestamp: number[];
    metricValueGroups: MetricValueGroup[];
  }

  export interface MetricValueGroup {
    metricValues: MetricValue[];
    tags: TagValue[];
  }

  export interface MetricValue {
    chartType: string;
    fieldName: string;
    unit: string;
    valueList: number[];
    tagList?: unknown[];
  }

  export interface TagValue {
    name: string;
    value: string;
  }
}
