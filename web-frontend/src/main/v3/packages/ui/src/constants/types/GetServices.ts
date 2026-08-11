export namespace GetServices {
  export type Response = string[];
}

export namespace PostService {
  export interface Body {
    serviceName: string;
  }
  export interface Response {
    result: string;
  }
}

export namespace DeleteService {
  export interface Parameters {
    serviceName: string;
  }
  export interface Response {
    result: string;
  }
}
