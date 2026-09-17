import type { TFunction } from 'i18next';

export const formatOverrideKey = (key: string, t: TFunction) => {
  const labelMap: Record<string, string> = {
    name: t('CONFIGURATION.ALARM_V2.OVERRIDE_NAME'),
    description: t('CONFIGURATION.ALARM_V2.OVERRIDE_DESCRIPTION'),
    severity: t('CONFIGURATION.ALARM_V2.OVERRIDE_SEVERITY'),
    checkIntervalSec: t('CONFIGURATION.ALARM_V2.OVERRIDE_CHECK_INTERVAL'),
    actionIntervalSec: t('CONFIGURATION.ALARM_V2.OVERRIDE_ACTION_INTERVAL'),
    conditions: t('CONFIGURATION.ALARM_V2.OVERRIDE_CONDITIONS'),
    filters: t('CONFIGURATION.ALARM_V2.OVERRIDE_FILTERS'),
  };
  return labelMap[key] ?? key;
};
