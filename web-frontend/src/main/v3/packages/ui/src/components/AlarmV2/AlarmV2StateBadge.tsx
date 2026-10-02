import { useTranslation } from 'react-i18next';
import { Badge } from '@pinpoint-fe/ui/src/components/ui';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';

export interface AlarmV2StateBadgeProps {
  status: 'NORMAL' | 'FIRING' | 'CHECK_FAILED';
}

export const AlarmV2StateBadge = ({ status }: AlarmV2StateBadgeProps) => {
  const { t } = useTranslation();
  return (
    <Badge
      variant="outline"
      className={cn(
        'text-xs font-medium',
        status === 'FIRING' && 'border-red-300 bg-red-50 text-red-700',
        status === 'CHECK_FAILED' && 'border-orange-300 bg-orange-50 text-orange-700',
        status === 'NORMAL' && 'border-green-300 bg-green-50 text-green-700',
      )}
    >
      {t(`CONFIGURATION.ALARM_V2.STATUS_${status}`)}
    </Badge>
  );
};

export interface AlarmV2SeverityBadgeProps {
  severity: 'CRITICAL' | 'WARNING';
}

export const AlarmV2SeverityBadge = ({ severity }: AlarmV2SeverityBadgeProps) => {
  const { t } = useTranslation();
  return (
    <Badge
      variant="outline"
      className={cn(
        'text-xs font-medium',
        severity === 'CRITICAL'
          ? 'border-status-fail bg-red-50 text-status-fail'
          : 'border-status-warn bg-orange-50 text-status-warn',
      )}
    >
      {t(`CONFIGURATION.ALARM_V2.SEVERITY_${severity}`)}
    </Badge>
  );
};
