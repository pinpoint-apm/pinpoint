import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import type { FormValues } from './AlarmV2RuleForm';

/** Where the rule is being saved -- identity the form itself does not carry. */
export interface RuleSaveContext {
  serviceName: string;
  applicationName: string;
  applicationType?: string;
}

/**
 * Turns form values into the save payload.
 *
 * For a rule linked to a bundle item, which fields are sent is the whole point: an
 * omitted field stays null on the rule and that is what makes it keep following the
 * item. Sending the inherited value back instead freezes a copy -- and nothing on
 * screen shows it, because the override badges are drawn from `overrideKeys` as the
 * server reports them.
 */
export const buildRuleSaveData = (
  values: FormValues,
  { serviceName, applicationName, applicationType }: RuleSaveContext,
): AlarmV2Rule.RuleSaveData => {
  if (!applicationType) {
    throw new Error('applicationType is required');
  }

  const base = {
    serviceName,
    applicationName,
    applicationType,
    enabled: values.enabled,
    templateItemId: values.templateItemId ?? null,
  };

  if (!values.templateItemId) {
    return {
      ...base,
      name: values.name,
      description: values.description,
      severity: values.severity,
      dataSource: values.dataSource,
      checkIntervalSec: values.checkIntervalSec,
      actionIntervalSec: values.actionIntervalSec,
      conditions: values.conditions,
      // Both branches default to []: an undefined filters key is dropped by
      // JSON.stringify, and the server reads a missing field as "leave as is",
      // so clearing every filter would silently keep the old ones.
      filters: values.filters ?? [],
    };
  }

  const overrideKeys = new Set(values.overrideKeys ?? []);
  return {
    ...base,
    dataSource: values.dataSource,
    ...(overrideKeys.has('name') && { name: values.name }),
    ...(overrideKeys.has('description') && { description: values.description }),
    ...(overrideKeys.has('severity') && { severity: values.severity }),
    ...(overrideKeys.has('checkIntervalSec') && { checkIntervalSec: values.checkIntervalSec }),
    ...(overrideKeys.has('actionIntervalSec') && { actionIntervalSec: values.actionIntervalSec }),
    ...(overrideKeys.has('conditions') && { conditions: values.conditions }),
    ...(overrideKeys.has('filters') && { filters: values.filters ?? [] }),
  };
};
