export namespace AlarmV2Rule {
  export enum ConditionType {
    GROUP = 'GROUP',
    LEAF = 'LEAF',
  }

  export interface AlarmCondition {
    type: ConditionType;
    // leaf fields
    metric?: string;
    op?: string;
    threshold?: number;
    windowSec?: number;
    aggregation?: string;
    trigger?: string;
    // group fields
    operator?: 'AND' | 'OR';
    criteria?: AlarmCondition[];
  }

  export interface AlarmFilter {
    key: string;
    op: 'EQ' | 'NEQ' | 'CONTAINS' | 'NOT_CONTAINS';
    value: string;
  }

  export interface RuleData {
    id?: number;
    name: string;
    description?: string;
    severity: 'CRITICAL' | 'WARNING';
    /** Which storage the rule measures. The values come from the datasources endpoint,
     *  since each installed module contributes its own. */
    dataSource: string;
    serviceName: string;
    applicationName: string;
    applicationType: string;
    checkIntervalSec: number;
    actionIntervalSec: number;
    conditions: AlarmCondition;
    filters?: AlarmFilter[];
    enabled: boolean;
    updatedAt?: string;
    // The stored link is the bundle item; the bundle id/name and the item name
    // are resolved by the server for display only.
    templateItemId?: number;
    templateItemName?: string;
    templateId?: number;
    templateName?: string;
    overrideKeys?: string[];
    localConfig?: RuleLocalConfig | null;
    templateItem?: AlarmV2Template.TemplateItemData | null;
    usedRuleCount?: number;
  }

  export interface RuleLocalConfig {
    ruleId: number;
    severity?: RuleData['severity'];
    checkIntervalSec?: number;
    actionIntervalSec?: number;
    conditions?: AlarmCondition;
    filters?: AlarmFilter[];
  }

  export interface RuleSaveData {
    /** Omitted on a template-linked rule that inherits the bundle item's text. */
    name?: string;
    description?: string;
    severity?: RuleData['severity'];
    dataSource?: RuleData['dataSource'];
    serviceName: string;
    applicationName: string;
    applicationType: string;
    checkIntervalSec?: number;
    actionIntervalSec?: number;
    conditions?: AlarmCondition;
    filters?: AlarmFilter[];
    enabled: boolean;
    templateItemId?: number | null;
  }

  /** Read-endpoint query string. serviceName is not here: it rides the pServiceName header. */
  export interface Parameters {
    applicationName?: string;
    applicationType?: string;
  }

  export type Response = RuleData[];

  export interface StateResponse {
    ruleId: number;
    status: 'NORMAL' | 'FIRING' | 'CHECK_FAILED';
    lastCheckedAt?: string;
    lastFiredAt?: string;
    lastNotifiedAt?: string;
  }

  export interface HistoryEntry {
    id: number;
    ruleId: number;
    eventType: 'FIRED' | 'RESOLVED' | 'CHECK_FAILED';
    message: string;
    context?: string;
    createdAt: string;
  }
}

export namespace AlarmV2Catalog {
  export interface MetricDefinition {
    value: string;
    label: string;
    trigger?: string;
    allowedAggregations?: string[];
  }

  export interface DataSourceDefinition {
    value: string;
    label: string;
    category: string;
    filterKeys: string[];
    metrics: MetricDefinition[];
  }
}

export namespace AlarmV2Channel {
  export interface Config {
    format?: 'DEFAULT' | 'SLACK';
    title?: string;
    template?: string;
    [key: string]: unknown;
  }

  export interface ChannelData {
    id?: number;
    serviceName?: string;
    channelName: string;
    methodType: 'SMS' | 'EMAIL' | 'WEBHOOK';
    destination: string;
    config?: Config | null;
    webhookAlias?: string;
    webhookUrl?: string;
    updatedAt?: string;
    templateCount?: number;
    affectedRuleCount?: number;
    enabledAffectedRuleCount?: number;
  }

  export type ChannelWriteData = Omit<
    ChannelData,
    | 'id'
    | 'serviceName'
    | 'updatedAt'
    | 'templateCount'
    | 'affectedRuleCount'
    | 'enabledAffectedRuleCount'
  >;

  export type Response = ChannelData[];
}

export namespace AlarmV2Template {
  /** One rule definition inside a bundle. */
  export interface TemplateItemData {
    id?: number;
    templateId?: number;
    name: string;
    description?: string;
    severity: AlarmV2Rule.RuleData['severity'];
    dataSource: AlarmV2Rule.RuleData['dataSource'];
    checkIntervalSec: number;
    actionIntervalSec: number;
    conditions: AlarmV2Rule.AlarmCondition;
    filters?: AlarmV2Rule.AlarmFilter[];
    updatedAt?: string;
    usedRuleCount?: number;
    enabledUsedRuleCount?: number;
  }

  /** Bundle header: identity that channels bind to and applications apply. */
  export interface TemplateData {
    id?: number;
    serviceName: string;
    name: string;
    description?: string;
    items: TemplateItemData[];
    updatedAt?: string;
    usedRuleCount?: number;
    enabledUsedRuleCount?: number;
    usedApplicationCount?: number;
    channelCount?: number;
  }

  export type Response = TemplateData[];
}

export namespace AlarmV2TemplatePreset {
  /** Preset display texts ship in both locales; the UI picks the active one. */
  export interface LocalizedText {
    ko: string;
    en: string;
  }

  export interface PresetRule {
    name: LocalizedText;
    description?: LocalizedText;
    severity: AlarmV2Rule.RuleData['severity'];
    dataSource: AlarmV2Rule.RuleData['dataSource'];
    checkIntervalSec: number;
    actionIntervalSec: number;
    conditions: AlarmV2Rule.AlarmCondition;
    filters?: AlarmV2Rule.AlarmFilter[];
  }

  export interface PresetData {
    name: LocalizedText;
    description: LocalizedText;
    rules: PresetRule[];
  }

  export type Response = PresetData[];
}
