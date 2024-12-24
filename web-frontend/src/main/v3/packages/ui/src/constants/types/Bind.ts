export namespace Bind {
  export interface Parameters {
    type: string;
    metaData: string;
    bind: string;
  }

  export interface Response {
    bindedQuery: string;
  }
}
