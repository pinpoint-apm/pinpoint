export namespace AlarmRule {
  export interface Parameters {
    applicationName: string;
  }

  export interface AlarmRuleData {
    ruleId?: string;
    applicationName: string;
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

  export interface DeleteParmeters extends Pick<
    AlarmRuleData,
    'applicationName' | 'emailSend' | 'ruleId' | 'smsSend' | 'webhookSend'
  > {}

  export interface MutaionResponse {
    result: string;
  }
}
