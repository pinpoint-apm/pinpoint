export namespace OtlpMetricDefUserDefined {
  export interface GetParameters {
    applicationName: string;
  }

  export interface GetResponse {
    applicationName: string;
    appMetricDefinitionList: Metric[];
  }

  export interface PatchResponse {
    result: string;
  }

  export interface PatchParameters {
    applicationName: string;
    appMetricDefinitionList: Metric[];
  }

  export interface Metric {
    id?: string;
    applicationName: string;
    title: string;
    metricGroupName: string;
    metricName: string;
    primaryForFieldAndTagRelation: 'tag' | 'field';
    fieldNameList: string[];
    tagGroupList: string[];
    unit?: string;
    stack: boolean;
    stackDetails?: {
      showTotal?: boolean;
    };
    samplingInterval: number;
    chartType: string;
    layout: Layout;
    aggregationFunction: string;
    schemaVersion?: number;
  }

  export interface Layout {
    x: number;
    y: number;
    w: number;
    h: number;
  }
}
