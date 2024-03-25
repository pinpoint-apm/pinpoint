export namespace SystemMetricMetricInfo {
  export interface Parameters {
    hostGroupName: string;
    hostName: string;
  }
  export type Response = MetricInfoData[];
  export interface MetricInfoData {
    metricDefinitionId: string;
    tagGroup: boolean;
  }
}
