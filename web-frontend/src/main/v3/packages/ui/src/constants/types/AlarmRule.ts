export namespace AlarmRule {
  export interface Parameters {
    applicationId: string;
  }

  export interface AlarmRuleData {
    ruleId?: string;
    applicationId: string;
    serviceType: string;
    checkerName: string;
    threshold: number;
    userGroupId: string;
    smsSend: boolean;
    emailSend: boolean;
    webhookSend: boolean;
    notes: string;
  }

  export type Response = AlarmRuleData[];

  export interface PostParameters extends Omit<AlarmRuleData, 'ruleId'> {}

  export interface PutParameters extends AlarmRuleData {}

  export interface DeleteParmeters
    extends Pick<
      AlarmRuleData,
      'applicationId' | 'emailSend' | 'ruleId' | 'smsSend' | 'webhookSend'
    > {}

  export interface MutaionResponse {
    result: string;
  }
}
