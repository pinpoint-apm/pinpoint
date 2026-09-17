import React from 'react';
import { useTranslation } from 'react-i18next';
import { RxChevronRight } from 'react-icons/rx';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { type RowFilterInfo } from '@pinpoint-fe/ui/src/components/DataTable';
import { cn } from '@pinpoint-fe/ui/src/lib';
import { AlarmV2SeverityBadge } from './AlarmV2StateBadge';
import { AlarmV2TableActions } from './AlarmV2TableActions';
import { formatCondition, formatIntervalShort } from './formatCondition';
import { AlarmV2EmptyState } from './AlarmV2EmptyState';
import { useAlarmV2CatalogLabels } from '@pinpoint-fe/ui/src/hooks/utility/useAlarmV2CatalogLabels';

import {
  CHEVRON,
  EXPANSION_ACCENT,
  TABLE_CELL,
  TABLE_HEADER_CELLS,
  TABLE_MIN_WIDTH,
} from './tableStyles';

export interface AlarmV2TemplateTableProps {
  data?: AlarmV2Template.TemplateData[];
  disabled?: boolean;
  rowFilterInfo?: RowFilterInfo;
  onClickRowItem?: (data: AlarmV2Template.TemplateData) => void;
  onClickEdit?: (data: AlarmV2Template.TemplateData) => void;
  onClickDelete?: (data: AlarmV2Template.TemplateData) => void;
}

/**
 * A bundle holds N rule definitions, so each row expands to show them. DataTable has no
 * row expansion, hence the hand-rolled table.
 */
export const AlarmV2TemplateTable = ({
  data,
  disabled,
  rowFilterInfo,
  onClickRowItem,
  onClickEdit,
  onClickDelete,
}: AlarmV2TemplateTableProps) => {
  const { t } = useTranslation();
  const { dataSourceLabel, metricLabel } = useAlarmV2CatalogLabels();
  const [openIds, setOpenIds] = React.useState<string[]>([]);
  const query = (rowFilterInfo?.query ?? '').trim().toLowerCase();

  const matchesQuery = React.useCallback(
    (text?: string) => !!text && text.toLowerCase().includes(query),
    [query],
  );

  /**
   * Searching for an item name shows that item, not everything sitting in the same
   * bundle -- otherwise the search hands back rules the user did not ask for. Matching
   * the bundle itself is different: the bundle is what was found, so it keeps every
   * item.
   */
  const visibleItems = React.useCallback(
    (template: AlarmV2Template.TemplateData) => {
      const items = template.items ?? [];
      if (!query || matchesQuery(template.name) || matchesQuery(template.description)) {
        return items;
      }
      return items.filter((item) => matchesQuery(item.name));
    },
    [query, matchesQuery],
  );

  const templates = React.useMemo(() => {
    if (!query) {
      return data ?? [];
    }
    return (data ?? []).filter(
      (template) =>
        matchesQuery(template.name) ||
        matchesQuery(template.description) ||
        (template.items ?? []).some((item) => matchesQuery(item.name)),
    );
  }, [data, query, matchesQuery]);

  /**
   * A search that hides the sibling items but leaves the bundle collapsed still costs
   * a click to see what matched. Seeding the open set rather than deriving it keeps
   * the toggle working: a bundle opened this way can be collapsed again.
   */
  const matchedKeys = templates
    .map((template) => String(template.id ?? template.name))
    .join('\u0000');
  React.useEffect(() => {
    if (!query) {
      return;
    }
    setOpenIds((prev) => Array.from(new Set([...prev, ...matchedKeys.split('\u0000')])));
  }, [query, matchedKeys]);

  const toggle = (key: string) =>
    setOpenIds((prev) => (prev.includes(key) ? prev.filter((id) => id !== key) : [...prev, key]));

  if (!templates.length) {
    return <AlarmV2EmptyState>{t('CONFIGURATION.ALARM_V2.NO_TEMPLATES')}</AlarmV2EmptyState>;
  }

  return (
    <div className="overflow-x-auto rounded-md border">
      <table className={cn('w-full text-sm', TABLE_MIN_WIDTH)}>
        <thead className={TABLE_HEADER_CELLS}>
          <tr className="border-b">
            <th>{t('CONFIGURATION.COMMON.NAME')}</th>
            {/* 28rem, not 26rem: the inner Severity column has to start here, and the
                inner fixed columns total 32px more than the outer ones. Widening Used by
                pulls its left edge left by exactly that much. */}
            <th className="w-[28rem]">{t('CONFIGURATION.ALARM_V2.USED_BY')}</th>
            <th className="w-28">{t('CONFIGURATION.ALARM_V2.CHANNELS')}</th>
            <th className="w-[72px] !text-center">{t('CONFIGURATION.ALARM_V2.ACTIONS')}</th>
          </tr>
        </thead>
        <tbody>
          {templates.map((template) => {
            const key = String(template.id ?? template.name);
            const open = openIds.includes(key);
            return (
              <React.Fragment key={key}>
                <tr
                  className="cursor-pointer border-b last:border-b-0 hover:bg-muted/40"
                  onClick={() => toggle(key)}
                >
                  <td className={TABLE_CELL}>
                    <div className="flex items-start gap-2">
                      <RxChevronRight className={cn(CHEVRON, 'mt-0.5', open && 'rotate-90')} />
                      <div className="min-w-0">
                        <button
                          type="button"
                          className="block w-full truncate text-left font-medium hover:underline"
                          onClick={(event) => {
                            if (!onClickRowItem) return;
                            event.stopPropagation();
                            onClickRowItem(template);
                          }}
                        >
                          {template.name}
                        </button>
                        {template.description && (
                          <div className="truncate text-xs text-muted-foreground">
                            {template.description}
                          </div>
                        )}
                      </div>
                    </div>
                  </td>
                  {/* One grid, like the channel table's Used by: fixed-width numeric
                      slots keep the counts lined up down the column. */}
                  <td className={cn(TABLE_CELL, 'text-muted-foreground')}>
                    <span className="flex items-baseline">
                      {(
                        [
                          [
                            'CONFIGURATION.ALARM_V2.DEFINITION_COUNT_LABEL',
                            String(template.items?.length ?? 0),
                          ],
                          [
                            'CONFIGURATION.ALARM_V2.APPLICATION_COUNT_LABEL',
                            String(template.usedApplicationCount ?? 0),
                          ],
                          [
                            'CONFIGURATION.ALARM_V2.ENABLED_SHORT',
                            `${template.enabledUsedRuleCount ?? 0}/${template.usedRuleCount ?? 0}`,
                          ],
                        ] as const
                      ).map(([labelKey, value], index) => (
                        <span
                          key={labelKey}
                          className={cn(
                            'flex items-baseline gap-1.5 pr-3',
                            index > 0 && 'border-l border-border pl-3',
                          )}
                        >
                          <span>{t(labelKey)}</span>
                          <span className="w-10 text-right tabular-nums">{value}</span>
                        </span>
                      ))}
                    </span>
                  </td>
                  <td className={cn(TABLE_CELL, 'text-muted-foreground')}>
                    {template.channelCount
                      ? t('CONFIGURATION.ALARM_V2.CHANNEL_COUNT', { count: template.channelCount })
                      : '-'}
                  </td>
                  <td className={cn(TABLE_CELL, 'text-center')}>
                    <AlarmV2TableActions
                      data={template}
                      actions={[
                        { label: t('COMMON.EDIT'), disabled, onClick: onClickEdit },
                        {
                          label: t('COMMON.DELETE'),
                          disabled,
                          destructive: true,
                          separatorBefore: true,
                          onClick: onClickDelete,
                        },
                      ]}
                    />
                  </td>
                </tr>
                {open && (
                  <tr className="border-b last:border-b-0">
                    <td colSpan={4} className={cn(EXPANSION_ACCENT, 'bg-muted/25 p-0')}>
                      {visibleItems(template).length ? (
                        // Fixed layout so Name takes exactly the remainder, i.e. the outer Name
                        // column, and Severity starts under Used by. Column widths match the
                        // Rules table; Conditions = outer fixed columns (28rem + 7rem + 4.5rem)
                        // minus the inner fixed ones (7rem + 8rem + 7rem).
                        <table className="w-full table-fixed text-sm [&_td]:px-2 [&_td]:py-2 [&_th]:px-2 [&_td:first-child]:pl-8 [&_th:first-child]:pl-8">
                          <thead>
                            {/* Same size as the rows below: depth is already said by the
                                indent, the expansion accent and the panel background, and a
                                header smaller than its own body has nothing to anchor a scan.
                                h-10 matches the outer header exactly -- the two-line Interval
                                cell would otherwise push this row past it, and a nested header
                                taller than the one it sits under reads as the senior of the two. */}
                            <tr className="h-10 border-b align-middle text-sm text-muted-foreground">
                              <th className="text-left font-medium">
                                {t('CONFIGURATION.COMMON.NAME')}
                              </th>
                              <th className="w-28 text-left font-medium">
                                {t('CONFIGURATION.ALARM_V2.SEVERITY')}
                              </th>
                              <th className="w-32 text-left font-medium">
                                {t('CONFIGURATION.ALARM_V2.DATA_SOURCE')}
                              </th>
                              <th className="w-28 text-left font-medium leading-tight">
                                {t('CONFIGURATION.ALARM_V2.INTERVAL')}
                                <span className="block text-[10px] font-normal opacity-70">
                                  {t('CONFIGURATION.ALARM_V2.INTERVAL_CHECK_ACTION')}
                                </span>
                              </th>
                              <th className="w-[17.5rem] text-left font-medium">
                                {t('CONFIGURATION.ALARM_V2.OVERRIDE_CONDITIONS')}
                              </th>
                            </tr>
                          </thead>
                          <tbody>
                            {visibleItems(template).map((item) => (
                              <tr key={item.id ?? item.name} className="border-b last:border-b-0">
                                <td className="truncate font-medium">{item.name}</td>
                                <td>
                                  <AlarmV2SeverityBadge severity={item.severity} />
                                </td>
                                <td className="text-muted-foreground">
                                  {dataSourceLabel(item.dataSource)}
                                </td>
                                <td className="text-muted-foreground">
                                  {formatIntervalShort(item.checkIntervalSec)} /{' '}
                                  {formatIntervalShort(item.actionIntervalSec)}
                                </td>
                                {/* Fixed width, so long conditions truncate; the full text is
                                    in the tooltip, as in the Rules table. */}
                                <td className="truncate text-muted-foreground">
                                  <span title={formatCondition(item.conditions, metricLabel, t)}>
                                    {formatCondition(item.conditions, metricLabel, t)}
                                  </span>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      ) : (
                        <p className="px-8 py-3 text-xs text-muted-foreground">
                          {t('CONFIGURATION.ALARM_V2.NO_ITEMS')}
                        </p>
                      )}
                    </td>
                  </tr>
                )}
              </React.Fragment>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
