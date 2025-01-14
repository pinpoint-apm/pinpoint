export namespace InspectorAgentStatusTimelineType {
  export interface Parameters {
    applicationName: string;
    agentId: string;
    from: number;
    to: number;
    exclude: string;
  }

  export interface Response {
    agentEventTimeline: {
      timelineSegments: {
        startTimestamp: number;
        endTimestamp: number;
        value: {
          totalCount: number;
        };
      }[];
    };
    agentStatusTimeline: {
      includeWarning: boolean;
      timelineSegments: {
        startTimestamp: number;
        endTimestamp: number;
        value: string;
      }[];
    };
  }
}
