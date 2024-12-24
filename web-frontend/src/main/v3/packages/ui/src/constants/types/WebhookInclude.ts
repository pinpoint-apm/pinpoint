import { AlarmRule } from './AlarmRule';

export namespace WebhookInclude {
  export interface Parameters {
    rule: AlarmRule.AlarmRuleData;
    webhookIds: string[];
  }
  export interface Response {
    result: string;
  }
}
