export namespace OtlpMetricDefProperty {
  export interface Response {
    metricGroupList: MetricGroupList[];
    chartTypeList: string[];
    aggregationFunctionList: string[];
  }

  export interface Parameters {
    applicationName: string;
  }

  export interface MetricGroupList {
    metricGroupName: string;
    metricList: MetricList[];
  }

  export interface MetricList {
    metricName: string;
    tagClusterList: TagCluster[];
    fieldClusterList: FieldCluster[];
  }

  export interface TagCluster {
    tagGroup: string;
    fieldAndUnitList: FieldAndUnit[];
  }

  export interface FieldAndUnit {
    fieldName: string;
    unit: string;
  }

  export interface FieldCluster {
    fieldName: string;
    unit: string;
    tagGroupList: string[];
  }
}
