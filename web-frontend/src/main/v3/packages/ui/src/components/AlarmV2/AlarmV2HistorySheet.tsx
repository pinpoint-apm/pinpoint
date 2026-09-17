import { useTranslation } from 'react-i18next';
import { format } from '@pinpoint-fe/ui';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { Badge } from '@pinpoint-fe/ui/src/components/ui/badge';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';
import { AlarmV2StateBadge } from './AlarmV2StateBadge';
import { AlarmV2Sheet } from './AlarmV2Sheet';

interface HistoryResult {
  metric: string;
  value: number | null;
  op: string;
  threshold: number | null;
}

interface HistoryContext {
  results?: HistoryResult[];
}

const RATE_METRICS = new Set(['crash_free_session_rate', 'session_error_rate']);
const RATE_NUMBER_FORMATTER = new Intl.NumberFormat(undefined, {
  maximumFractionDigits: 3,
});

export interface AlarmV2HistorySheetProps {
  rule?: AlarmV2Rule.RuleData;
  history?: AlarmV2Rule.HistoryEntry[];
  status?: AlarmV2Rule.StateResponse['status'];
  onOpenChange: (open: boolean) => void;
}

const parseHistoryResults = (context?: string): HistoryResult[] => {
  if (!context) {
    return [];
  }

  try {
    const parsed = JSON.parse(context) as HistoryContext;
    return Array.isArray(parsed.results) ? parsed.results : [];
  } catch {
    return [];
  }
};

const formatCreatedAt = (createdAt: string) => {
  const date = new Date(createdAt);
  return Number.isNaN(date.getTime()) ? createdAt : format(date, 'MMM d, yyyy, HH:mm:ss');
};

const formatHistoryMetricValue = (metric: string, value?: number | null): string => {
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    return '-';
  }

  if (RATE_METRICS.has(metric)) {
    return `${RATE_NUMBER_FORMATTER.format(value)}%`;
  }

  return String(value);
};

export const AlarmV2HistorySheet = ({
  rule,
  history,
  status,
  onOpenChange,
}: AlarmV2HistorySheetProps) => {
  const { t } = useTranslation();

  return (
    <AlarmV2Sheet
      open={!!rule}
      onOpenChange={onOpenChange}
      bodyClassName="space-y-3"
      title={
        <span className="flex items-center gap-2">
          {t('CONFIGURATION.ALARM_V2.RULE_HISTORY')}
          {rule?.name && (
            <span className="text-muted-foreground font-normal text-base">{rule.name}</span>
          )}
          {status && <AlarmV2StateBadge status={status} />}
        </span>
      }
    >
      {history?.length === 0 && (
        <p className="text-sm text-muted-foreground text-center py-8">
          {t('CONFIGURATION.ALARM_V2.EMPTY_HISTORY')}
        </p>
      )}
      {history?.map((entry) => (
        <AlarmV2HistoryItem key={entry.id} entry={entry} />
      ))}
    </AlarmV2Sheet>
  );
};

const AlarmV2HistoryItem = ({ entry }: { entry: AlarmV2Rule.HistoryEntry }) => {
  const { t } = useTranslation();
  const results = parseHistoryResults(entry.context);

  return (
    <div className="flex flex-col gap-1.5 p-3 rounded-md border bg-card">
      <div className="flex items-center justify-between gap-2">
        <Badge
          variant="outline"
          className={cn(
            'text-xs font-medium',
            entry.eventType === 'FIRED' && 'border-red-300 bg-red-50 text-red-700',
            entry.eventType === 'CHECK_FAILED' && 'border-orange-300 bg-orange-50 text-orange-700',
            entry.eventType === 'RESOLVED' && 'border-green-300 bg-green-50 text-green-700',
          )}
        >
          {t(`CONFIGURATION.ALARM_V2.${entry.eventType}`)}
        </Badge>
        <span className="text-xs text-muted-foreground">{formatCreatedAt(entry.createdAt)}</span>
      </div>
      <p className="text-sm">{entry.message}</p>
      {results.length > 0 && <AlarmV2HistoryResults results={results} />}
    </div>
  );
};

const AlarmV2HistoryResults = ({ results }: { results: HistoryResult[] }) => {
  const { t } = useTranslation();

  return (
    <div className="mt-1">
      <p className="text-xs font-medium text-muted-foreground mb-1">
        {t('CONFIGURATION.ALARM_V2.FIRED_CONTEXT')}
      </p>
      <table className="w-full text-xs border-collapse">
        <thead>
          <tr className="text-muted-foreground">
            <th className="text-left font-medium pb-1 pr-3">
              {t('CONFIGURATION.ALARM_V2.METRIC')}
            </th>
            <th className="text-right font-medium pb-1 pr-3">
              {t('CONFIGURATION.ALARM_V2.ACTUAL_VALUE')}
            </th>
            <th className="text-center font-medium pb-1 pr-3">
              {t('CONFIGURATION.ALARM_V2.FILTER_OP')}
            </th>
            <th className="text-right font-medium pb-1">{t('CONFIGURATION.ALARM_V2.THRESHOLD')}</th>
          </tr>
        </thead>
        <tbody>
          {results.map((result, index) => (
            <tr key={`${result.metric}-${index}`} className="border-t border-border/50">
              <td className="py-0.5 pr-3 font-mono">{result.metric}</td>
              <td className="py-0.5 pr-3 text-right font-medium text-red-600">
                {formatHistoryMetricValue(result.metric, result.value)}
              </td>
              <td className="py-0.5 pr-3 text-center text-muted-foreground">{result.op}</td>
              <td className="py-0.5 text-right text-muted-foreground">
                {formatHistoryMetricValue(result.metric, result.threshold)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
