export type ErrorResponse = {
  timestamp: number;
  status: number;
  error: string;
  exception: string;
  trace: string;
  message: string;
  path: string;
  data: {
    requestInfo: {
      method: string;
      url: string;
      headers: { [key: string]: string[] };
      parameters: { [key: string]: string[] };
    };
  };
};
