export namespace ConfigAgentDuplicationCheck {
  export interface Parameters {
    agentId: string;
  }

  export type Response = {
    code: number;
    message: string;
  };
}
