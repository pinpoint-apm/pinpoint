import React from 'react';
import { useTranslation } from 'react-i18next';
import { MdOutlineLinkOff } from 'react-icons/md';
import { RxChevronRight } from 'react-icons/rx';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { type RowFilterInfo } from '@pinpoint-fe/ui/src/components/DataTable';
import { Badge } from '@pinpoint-fe/ui/src/components/ui';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { Switch } from '@pinpoint-fe/ui/src/components/ui/switch';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@pinpoint-fe/ui/src/components/ui/accordion';
import { AlarmV2SeverityBadge } from './AlarmV2StateBadge';
import { AlarmV2TableActions } from './AlarmV2TableActions';
import { formatOverrideKey } from './formatOverrideKey';
import { AlarmV2EmptyState } from './AlarmV2EmptyState';
import { useAlarmV2CatalogLabels } from '@pinpoint-fe/ui/src/hooks/utility/useAlarmV2CatalogLabels';

import { formatCondition, formatIntervalShort } from './formatCondition';
import {
  CHEVRON,
  CHEVRON_SPACER,
  EXPANSION_ACCENT,
  TABLE_HEADER_ROW,
  TABLE_MIN_WIDTH,
} from './tableStyles';

export interface AlarmV2RuleTableProps {
  data?: AlarmV2Rule.RuleData[];
  disabled?: boolean;
  rowFilterInfo?: RowFilterInfo;
  onClickRowItem?: (data: AlarmV2Rule.RuleData) => void;
  onClickEdit?: (data: AlarmV2Rule.RuleData) => void;
  onClickDelete?: (data: AlarmV2Rule.RuleData) => void;
  onClickHistory?: (data: AlarmV2Rule.RuleData) => void;
  onToggleEnabled?: (data: AlarmV2Rule.RuleData, enabled: boolean) => void;
  /** Removes every rule this application got from the bundle. */
  onUnlinkTemplate?: (templateId: number, templateName: string, ruleCount: number) => void;
}

interface RuleGroup {
  key: string;
  /** Absent on the standalone group, which has nothing to unlink. */
  templateId?: number;
  name: string;
  rules: AlarmV2Rule.RuleData[];
}

const STANDALONE_GROUP_KEY = 'standalone';

/**
 * Marks a cell whose value the rule overrides. An override is a property of the
 * value the user is looking at, not a separate column, and a standalone rule has
 * nothing to override, so it never renders one.
 */
const OverrideMark = ({
  rule,
  overrideKey,
}: {
  rule: AlarmV2Rule.RuleData;
  /** One key, or several when a cell shows more than one value (the interval pair). */
  overrideKey: string | string[];
}) => {
  const { t } = useTranslation();
  const keys = Array.isArray(overrideKey) ? overrideKey : [overrideKey];
  const overridden = keys.filter((key) => rule.overrideKeys?.includes(key));
  if (overridden.length === 0) {
    return null;
  }
  return (
    <span
      aria-label={t('CONFIGURATION.ALARM_V2.OVERRIDE')}
      title={`${t('CONFIGURATION.ALARM_V2.OVERRIDE')}: ${overridden
        .map((key) => formatOverrideKey(key, t))
        .join(', ')}`}
      className="absolute -left-3 top-1/2 size-1.5 -translate-y-1/2 rounded-full bg-primary"
    />
  );
};

const RuleRow = ({
  rule,
  disabled,
  onClickRowItem,
  onClickEdit,
  onClickDelete,
  onClickHistory,
  onToggleEnabled,
}: {
  rule: AlarmV2Rule.RuleData;
  disabled?: boolean;
} & Pick<
  AlarmV2RuleTableProps,
  'onClickRowItem' | 'onClickEdit' | 'onClickDelete' | 'onClickHistory' | 'onToggleEnabled'
>) => {
  const { t } = useTranslation();
  const { dataSourceLabel, metricLabel } = useAlarmV2CatalogLabels();
  return (
    <div
      className={cn(
        'flex cursor-pointer items-center gap-4 py-2 pr-2 text-sm hover:bg-accent/50',
        // Same padding on every row. Indenting with padding would narrow the row and
        // the two flex-1 columns would absorb the difference, shifting every fixed
        // column out of line with the header.
        'pl-2',
      )}
      onClick={() => onClickRowItem?.(rule)}
    >
      <span className="flex min-w-0 flex-1 items-center gap-2">
        {/* Stands in for the band's chevron so the name starts where the band's badge
            does. Colour follows the role of the cell, as in the Templates table: the
            name in foreground, descriptive attributes muted, state in its own colour. */}
        <span className={CHEVRON_SPACER} aria-hidden="true" />
        {/* The mark hangs off the text, not the cell: the cell starts at the chevron
            spacer, so anchoring there would float it away from the name it marks. */}
        <span className="relative min-w-0">
          <OverrideMark rule={rule} overrideKey="name" />
          <span className="block truncate font-medium">{rule.name}</span>
        </span>
      </span>
      <span className="relative flex w-28 shrink-0 items-center">
        <OverrideMark rule={rule} overrideKey="severity" />
        <AlarmV2SeverityBadge severity={rule.severity} />
      </span>
      <span className="w-32 shrink-0 text-muted-foreground">
        {dataSourceLabel(rule.dataSource)}
      </span>
      <span className="relative flex w-28 shrink-0 items-center text-muted-foreground">
        <OverrideMark rule={rule} overrideKey={['checkIntervalSec', 'actionIntervalSec']} />
        {formatIntervalShort(rule.checkIntervalSec)} / {formatIntervalShort(rule.actionIntervalSec)}
      </span>
      <span className="relative flex min-w-0 flex-1 items-center">
        <OverrideMark rule={rule} overrideKey="conditions" />
        <span className="truncate text-muted-foreground">
          {formatCondition(rule.conditions, metricLabel, t)}
        </span>
      </span>
      <span className="w-24 shrink-0" onClick={(event) => event.stopPropagation()}>
        <Switch
          checked={rule.enabled}
          disabled={disabled}
          onCheckedChange={(checked) => onToggleEnabled?.(rule, checked)}
        />
      </span>
      <span className="w-20 shrink-0 text-center" onClick={(event) => event.stopPropagation()}>
        <AlarmV2TableActions
          data={rule}
          actions={[
            { label: t('CONFIGURATION.ALARM_V2.HISTORY'), onClick: onClickHistory },
            { label: t('COMMON.EDIT'), disabled, separatorBefore: true, onClick: onClickEdit },
            {
              label: t('COMMON.DELETE'),
              disabled,
              destructive: true,
              separatorBefore: true,
              onClick: onClickDelete,
            },
          ]}
        />
      </span>
    </div>
  );
};

/**
 * Rules applied from a bundle are grouped under it, so the application shows
 * "which template gave me these" instead of a flat list. Standalone rules get a
 * band of their own so every rule row hangs under a band: with the bundles
 * collapsed, an indented row without a band above it read as an orphaned child.
 */
export const AlarmV2RuleTable = ({
  data,
  disabled,
  rowFilterInfo,
  onClickRowItem,
  onClickEdit,
  onClickDelete,
  onClickHistory,
  onToggleEnabled,
  onUnlinkTemplate,
}: AlarmV2RuleTableProps) => {
  const { t } = useTranslation();
  const query = (rowFilterInfo?.query ?? '').trim().toLowerCase();

  const filtered = React.useMemo(() => {
    if (!data) {
      return undefined;
    }
    if (!query) {
      return data;
    }
    return data.filter((rule) =>
      [rule.name, rule.description, rule.templateName, rule.templateItemName]
        .filter(Boolean)
        .some((text) => (text as string).toLowerCase().includes(query)),
    );
  }, [data, query]);

  // Unlinking deletes every rule the bundle produced for this application, not just
  // the ones the search left on screen, so the confirmation counts the unfiltered set.
  const ruleCountByTemplateId = React.useMemo(() => {
    const counts = new Map<number, number>();
    for (const rule of data ?? []) {
      if (rule.templateId) {
        counts.set(rule.templateId, (counts.get(rule.templateId) ?? 0) + 1);
      }
    }
    return counts;
  }, [data]);

  const groups = React.useMemo(() => {
    const groupMap = new Map<string, RuleGroup>();
    const standalone: AlarmV2Rule.RuleData[] = [];
    for (const rule of filtered ?? []) {
      if (!rule.templateId) {
        standalone.push(rule);
        continue;
      }
      const key = String(rule.templateId);
      const group = groupMap.get(key) ?? {
        key,
        templateId: rule.templateId,
        name: rule.templateName || `#${rule.templateId}`,
        rules: [],
      };
      group.rules.push(rule);
      groupMap.set(key, group);
    }
    const result = [...groupMap.values()];
    if (standalone.length) {
      result.push({
        key: STANDALONE_GROUP_KEY,
        name: t('CONFIGURATION.ALARM_V2.STANDALONE_RULES'),
        rules: standalone,
      });
    }
    return result;
  }, [filtered, t]);

  if (filtered === undefined) {
    return <AlarmV2EmptyState>{t('CONFIGURATION.ALARM_V2.SELECT_APP_FIRST')}</AlarmV2EmptyState>;
  }

  if (!groups.length) {
    return <AlarmV2EmptyState>{t('COMMON.NO_DATA')}</AlarmV2EmptyState>;
  }

  const rowProps = {
    disabled,
    onClickRowItem,
    onClickEdit,
    onClickDelete,
    onClickHistory,
    onToggleEnabled,
  };

  return (
    <div className="overflow-x-auto rounded-md border">
      <div className={TABLE_MIN_WIDTH}>
        <div className={TABLE_HEADER_ROW}>
          <span className="min-w-0 flex-1">{t('CONFIGURATION.COMMON.NAME')}</span>
          <span className="w-28 shrink-0">{t('CONFIGURATION.ALARM_V2.SEVERITY')}</span>
          <span className="w-32 shrink-0">{t('CONFIGURATION.ALARM_V2.DATA_SOURCE')}</span>
          <span className="w-28 shrink-0 leading-tight">
            {t('CONFIGURATION.ALARM_V2.INTERVAL')}
            <span className="block text-[10px] font-normal opacity-70">
              {t('CONFIGURATION.ALARM_V2.INTERVAL_CHECK_ACTION')}
            </span>
          </span>
          <span className="min-w-0 flex-1">{t('CONFIGURATION.ALARM_V2.OVERRIDE_CONDITIONS')}</span>
          <span className="w-24 shrink-0">{t('CONFIGURATION.ALARM_V2.ENABLED')}</span>
          <span className="w-20 shrink-0 text-center">{t('CONFIGURATION.ALARM_V2.ACTIONS')}</span>
        </div>

        {/* Remounting on a change in the set of groups lets defaultValue apply again, so
          a bundle applied while the list is on screen shows up expanded. */}
        <Accordion
          key={groups.map((group) => group.key).join('|')}
          type="multiple"
          defaultValue={groups.map((group) => group.key)}
        >
          {groups.map((group) => {
            const enabledCount = group.rules.filter((rule) => rule.enabled).length;
            // A local const keeps the narrowing alive inside the click handler below.
            const templateId = group.templateId;
            return (
              // The container draws the outer border; a border on the last band would double it.
              <AccordionItem key={group.key} value={group.key} className="last:border-b-0">
                {/* Trailing padding and column width match the header and rule rows so the
                  unlink icon lines up with the per-rule action buttons. */}
                <div className="flex h-12 items-center gap-4 px-2 hover:bg-accent/50 [&>h3]:min-w-0 [&>h3]:flex-1">
                  <AccordionTrigger className="w-full py-0 hover:no-underline [&>svg]:hidden [&[data-state=open]_.group-chevron]:rotate-90">
                    <span className="flex min-w-0 items-center gap-2">
                      <RxChevronRight className={cn('group-chevron', CHEVRON)} />
                      <Badge
                        variant="outline"
                        className="border-slate-300 bg-slate-50 text-slate-600"
                      >
                        {t(
                          templateId === undefined
                            ? 'CONFIGURATION.ALARM_V2.STANDALONE'
                            : 'CONFIGURATION.ALARM_V2.TEMPLATE',
                        )}
                      </Badge>
                      <span className="truncate text-sm font-medium">{group.name}</span>
                    </span>
                  </AccordionTrigger>
                  {/* The ratio sits under the Enabled column; the rule count is already
                    obvious from the rows below. */}
                  <span className="w-24 shrink-0 text-sm font-normal text-muted-foreground">
                    {t('CONFIGURATION.ALARM_V2.ENABLED_RULE_RATIO', {
                      count: enabledCount,
                      total: group.rules.length,
                    })}
                  </span>
                  <span className="flex w-20 shrink-0 justify-center">
                    {templateId !== undefined && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        disabled={disabled}
                        aria-label={t('CONFIGURATION.ALARM_V2.UNLINK_TEMPLATE')}
                        title={t('CONFIGURATION.ALARM_V2.UNLINK_TEMPLATE')}
                        onClick={() =>
                          onUnlinkTemplate?.(
                            templateId,
                            group.name,
                            ruleCountByTemplateId.get(templateId) ?? group.rules.length,
                          )
                        }
                      >
                        <MdOutlineLinkOff className="h-4 w-4 text-muted-foreground" />
                      </Button>
                    )}
                  </span>
                </div>
                <AccordionContent className="pb-0">
                  <div className={cn('divide-y divide-border/60 bg-muted/25', EXPANSION_ACCENT)}>
                    {group.rules.map((rule) => (
                      <RuleRow key={rule.id} rule={rule} {...rowProps} />
                    ))}
                  </div>
                </AccordionContent>
              </AccordionItem>
            );
          })}
        </Accordion>
      </div>
    </div>
  );
};
