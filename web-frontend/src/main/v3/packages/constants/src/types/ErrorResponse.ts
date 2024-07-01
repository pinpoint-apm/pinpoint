export type ErrorResponse = {
  timestamp: number;
  status: number;
  title: string;
  exception: string;
  trace: string;
  message: string;
  instance: string;
  data: {
    requestInfo: {
      method: string;
      headers: { [key: string]: string[] };
      parameters: { [key: string]: string[] };
    };
  };
};

export type ErrorDetailResponse = ErrorResponse & { url?: string };
