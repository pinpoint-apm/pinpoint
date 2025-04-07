export namespace GetHeatmapAppData {
  export interface Parameters {
    applicationName?: string;
    from?: number;
    to?: number;
    minElapsedTime?: number;
    maxElapsedTime?: number;
    agentId?: string;
  }

  export interface Response {
    size: {
      // 전체 메트릭 사이즈
      width: number;
      height: number;
    };
    summary: {
      totalSuccessCount: number;
      totalFailCount: number;
    };
    heatmapData: HeatmapData[];
  }

  export interface HeatmapData {
    column: number; // 열 번호
    timestamp: number; //x축 값, LONG형태 시간값 ex) 1743995520000
    cellDataList: CellData[];
  }

  export interface CellData {
    row: number; // 행 번호
    elapsedTime: number; // y 축값
    successCount: number;
    failCount: number;
  }
}
