export namespace Webhook {
  export interface Parameters {
    ruleId?: string;
    applicationName?: string;
  }
  export type Response = WebhookData[];
  export interface WebhookData {
    webhookId?: string;
    alias: string;
    url: string;
    applicationName: string;
    serviceName: string;
  }

  export interface PostParameters extends Omit<WebhookData, 'webhookId'> {}

  export interface PutParameters extends WebhookData {}

  export interface DeleteParmeters extends WebhookData {}

  export interface MutaionResponse {
    result: string;
    webhookId?: string;
  }
}
