import type { TFunction } from 'i18next';

// Keep these lists ascending and aligned with backend AlarmValidationConstants:
// CHECK_INTERVAL_SEC_OPTIONS, ACTION_INTERVAL_SEC_OPTIONS. The server rejects a value
// above the last entry, so adding one here without the backend makes saving fail.
export const CHECK_INTERVAL_SEC_OPTIONS = [60, 300, 600, 1800, 3600];
export const ACTION_INTERVAL_SEC_OPTIONS = [300, 600, 1800, 3600, 21600, 43200, 86400];

export const DEFAULT_CHECK_INTERVAL_SEC = 600;
export const DEFAULT_ACTION_INTERVAL_SEC = 1800;

export const formatIntervalSec = (sec: number, t: TFunction) => {
  if (sec % 3600 === 0) {
    return t('CONFIGURATION.ALARM_V2.INTERVAL_HOUR', { value: sec / 3600 });
  }
  if (sec % 60 === 0) {
    return t('CONFIGURATION.ALARM_V2.INTERVAL_MINUTE', { value: sec / 60 });
  }
  return t('CONFIGURATION.ALARM_V2.INTERVAL_SECOND', { value: sec });
};

// A value saved before these options existed would render as a blank select.
// Round up so the form shows a selectable option; keep anything above the last option as is
// rather than shortening it, matching how the server normalizes.
const roundUpToOption = (sec: number, optionsSec: number[]) =>
  optionsSec.find((option) => option >= sec) ?? sec;

export const roundUpCheckIntervalSec = (sec: number) =>
  roundUpToOption(sec, CHECK_INTERVAL_SEC_OPTIONS);

export const roundUpActionIntervalSec = (sec: number) =>
  roundUpToOption(sec, ACTION_INTERVAL_SEC_OPTIONS);
