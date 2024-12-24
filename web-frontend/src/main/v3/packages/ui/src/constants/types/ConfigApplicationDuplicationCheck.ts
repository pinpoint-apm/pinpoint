export namespace ConfigApplicationDuplicationCheck {
  export interface Parameters {
    applicationName: string;
  }

  export type Response = {
    code: number;
    message: string;
  };
}
