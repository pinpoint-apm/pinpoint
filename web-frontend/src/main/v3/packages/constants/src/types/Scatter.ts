export const SCATTER_DATA_TOTAL_KEY = '__scatter_data_total__';

export type ScatterData = {
  x: number;
  y: number;
  type: string;
  hidden: boolean;
  transactionId?: string;
  collectorAcceptTime?: number;
  agentId?: string;
};

export type ScatterDataByAgent = {
  curr: {
    [key: string]: ScatterData[] | undefined;
    [SCATTER_DATA_TOTAL_KEY]: ScatterData[] | undefined;
  };
  acc: {
    [key: string]: ScatterData[] | undefined;
    [SCATTER_DATA_TOTAL_KEY]: ScatterData[] | undefined;
  };
};
