// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace GetServices {
  export type Response = string[];
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace PostServices {
  export interface Body {
    serviceName: string;
  }
  export interface Response {
    result: string;
  }
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace DeleteService {
  export interface Parameters {
    serviceName: string;
  }
  export interface Response {
    result: string;
  }
}
