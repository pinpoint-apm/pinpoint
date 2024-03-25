export namespace SqlStatFilterOptions {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
  }

  export type Response = FilterOption[];
  export interface FilterOption {
    groupName: string;
    options: string[];
  }
}
