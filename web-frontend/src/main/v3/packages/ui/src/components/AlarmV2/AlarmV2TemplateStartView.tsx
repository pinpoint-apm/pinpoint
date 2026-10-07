import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  AlarmV2Rule,
  AlarmV2Template,
  AlarmV2TemplatePreset,
} from '@pinpoint-fe/ui/src/constants/types';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { Badge } from '@pinpoint-fe/ui/src/components/ui/badge';
import { Checkbox } from '@pinpoint-fe/ui/src/components/ui/checkbox';
import { conditionMetrics } from './formatCondition';
import { useAlarmV2CatalogLabels } from '@pinpoint-fe/ui/src/hooks/utility/useAlarmV2CatalogLabels';
import { useAlarmV2DataSourcesQuery } from '@pinpoint-fe/ui/src/hooks/api';

import { CHEVRON, EXPANSION_ACCENT } from './tableStyles';
import { RxChevronRight } from 'react-icons/rx';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@pinpoint-fe/ui/src/components/ui/accordion';

interface StartCandidate {
  key: string;
  title: string;
  subtitle?: string;
  items: AlarmV2Template.TemplateItemData[];
}

export interface AlarmV2TemplateStartViewProps {
  presets?: AlarmV2TemplatePreset.PresetData[];
  templates?: AlarmV2Template.TemplateData[];
  standaloneRules?: AlarmV2Rule.RuleData[];
  /** The items to start with, and the name of the source when only one was picked. */
  onContinue: (items: AlarmV2Template.TemplateItemData[], suggestedName?: string) => void;
  onCancel: () => void;
}

const toItemFromPresetRule = (
  rule: AlarmV2TemplatePreset.PresetRule,
  locale: 'ko' | 'en',
): AlarmV2Template.TemplateItemData => ({
  name: rule.name[locale],
  description: rule.description?.[locale],
  severity: rule.severity,
  dataSource: rule.dataSource,
  checkIntervalSec: rule.checkIntervalSec,
  actionIntervalSec: rule.actionIntervalSec,
  conditions: structuredClone(rule.conditions),
  filters: rule.filters ? structuredClone(rule.filters) : [],
});

const toItemFromRule = (rule: AlarmV2Rule.RuleData): AlarmV2Template.TemplateItemData => ({
  name: rule.name,
  description: rule.description,
  severity: rule.severity,
  dataSource: rule.dataSource,
  checkIntervalSec: rule.checkIntervalSec,
  actionIntervalSec: rule.actionIntervalSec,
  conditions: structuredClone(rule.conditions),
  filters: rule.filters ? structuredClone(rule.filters) : [],
});

/** Copies never carry an id, so saving always inserts new bundle items. */
const copyItem = (item: AlarmV2Template.TemplateItemData): AlarmV2Template.TemplateItemData => ({
  ...structuredClone(item),
  id: undefined,
  templateId: undefined,
  // The counts describe the bundle this was copied from. A copy always saves as an
  // insert, so carrying them over would lock its data source for no reason.
  usedRuleCount: undefined,
  enabledUsedRuleCount: undefined,
});

/**
 * One chip per rule, labelled with the metrics it watches. Rule names would repeat
 * the candidate title whenever the candidate is itself a single rule.
 */
const ItemPreview = ({ items }: { items: AlarmV2Template.TemplateItemData[] }) => {
  const { t } = useTranslation();
  const { metricLabel } = useAlarmV2CatalogLabels();
  if (!items.length) {
    return <p className="text-xs text-muted-foreground">{t('CONFIGURATION.ALARM_V2.NO_ITEMS')}</p>;
  }
  return (
    <span className="flex flex-wrap gap-1">
      {items.map((item, index) => {
        const metrics = conditionMetrics(item.conditions).map((metric) => metricLabel(metric));
        return (
          <Badge
            key={index}
            variant="outline"
            title={item.name}
            className="font-normal text-muted-foreground"
          >
            {metrics.length ? metrics.join(', ') : item.name}
          </Badge>
        );
      })}
    </span>
  );
};

export const AlarmV2TemplateStartView = ({
  presets,
  templates,
  standaloneRules,
  onContinue,
  onCancel,
}: AlarmV2TemplateStartViewProps) => {
  const { t, i18n } = useTranslation();
  const locale = i18n.language?.startsWith('ko') ? 'ko' : 'en';
  const [selectedKeys, setSelectedKeys] = React.useState<string[]>([]);
  const [openSections, setOpenSections] = React.useState<string[]>(['presets']);

  const presetCandidates: StartCandidate[] = React.useMemo(
    () =>
      (presets ?? []).map((preset, index) => ({
        key: `preset-${index}`,
        title: preset.name[locale],
        subtitle: preset.description[locale],
        items: preset.rules.map((rule) => toItemFromPresetRule(rule, locale)),
      })),
    [presets, locale],
  );

  const templateCandidates: StartCandidate[] = React.useMemo(
    () =>
      (templates ?? [])
        .filter((template) => template.id !== undefined)
        .map((template) => ({
          key: `template-${template.id}`,
          title: template.name,
          subtitle: template.description,
          items: (template.items ?? []).map(copyItem),
        })),
    [templates],
  );

  const ruleCandidates: StartCandidate[] = React.useMemo(
    () =>
      (standaloneRules ?? [])
        .filter((rule) => !rule.templateItemId && rule.id !== undefined)
        .map((rule) => ({
          key: `rule-${rule.id}`,
          title: rule.name,
          subtitle: rule.description,
          items: [toItemFromRule(rule)],
        })),
    [standaloneRules],
  );

  const allCandidates = React.useMemo(
    () => [...presetCandidates, ...templateCandidates, ...ruleCandidates],
    [presetCandidates, templateCandidates, ruleCandidates],
  );

  // A bundle measures one category, so once something is picked the others cannot join it.
  const { data: catalog } = useAlarmV2DataSourcesQuery();
  const categoryOf = (candidate: StartCandidate) =>
    catalog?.find((ds) => ds.value === candidate.items[0]?.dataSource)?.category;
  const selectedCategory = allCandidates
    .filter((candidate) => selectedKeys.includes(candidate.key))
    .map(categoryOf)
    .find(Boolean);

  const toggle = (key: string) =>
    setSelectedKeys((prev) =>
      prev.includes(key) ? prev.filter((item) => item !== key) : [...prev, key],
    );

  // The button names how many rules the next step will start with, not how many
  // sources were ticked — one preset can carry several.
  const selectedItemCount = React.useMemo(() => {
    const selected = new Set(selectedKeys);
    return allCandidates
      .filter((candidate) => selected.has(candidate.key))
      .reduce((total, candidate) => total + candidate.items.length, 0);
  }, [allCandidates, selectedKeys]);

  const handleContinue = () => {
    const selected = new Set(selectedKeys);
    const chosen = allCandidates.filter((candidate) => selected.has(candidate.key));
    // One source names the bundle it starts. Several have no one name between them.
    onContinue(
      chosen.flatMap((c) => c.items),
      chosen.length === 1 ? chosen[0].title : undefined,
    );
  };

  const renderCandidates = (candidates: StartCandidate[], emptyText: string) =>
    candidates.length ? (
      <div className="space-y-4">
        {candidates.map((candidate) => (
          <label key={candidate.key} className="flex cursor-pointer items-start gap-3">
            <Checkbox
              className="mt-0.5"
              checked={selectedKeys.includes(candidate.key)}
              disabled={
                !selectedKeys.includes(candidate.key) &&
                !!selectedCategory &&
                categoryOf(candidate) !== selectedCategory
              }
              onCheckedChange={() => toggle(candidate.key)}
            />
            <span className="min-w-0 flex-1 space-y-2">
              <span className="flex items-center gap-2">
                <span className="truncate text-sm font-medium">{candidate.title}</span>
                {/* A single rule is already obvious from the lone chip below. */}
                {candidate.items.length > 1 && (
                  <Badge variant="outline" className="whitespace-nowrap font-normal">
                    {t('CONFIGURATION.ALARM_V2.RULE_COUNT', { count: candidate.items.length })}
                  </Badge>
                )}
              </span>
              {candidate.subtitle && (
                <span className="block text-xs text-muted-foreground">{candidate.subtitle}</span>
              )}
              <ItemPreview items={candidate.items} />
            </span>
          </label>
        ))}
      </div>
    ) : (
      <p className="text-xs text-muted-foreground">{emptyText}</p>
    );

  const renderSection = (
    value: string,
    title: string,
    candidates: StartCandidate[],
    emptyText: string,
  ) => (
    <AccordionItem value={value} className="border-b-0">
      <AccordionTrigger className="bg-muted/40 px-3 py-1.5 hover:no-underline [&>svg]:hidden [&[data-state=open]_.section-chevron]:rotate-90">
        <span className="flex items-center gap-2">
          <RxChevronRight className={`section-chevron ${CHEVRON}`} />
          <span className="text-sm font-semibold">{title}</span>
          <span className="text-xs font-normal text-muted-foreground">({candidates.length})</span>
        </span>
      </AccordionTrigger>
      <AccordionContent className={`${EXPANSION_ACCENT} py-3 pl-3`}>
        {renderCandidates(candidates, emptyText)}
      </AccordionContent>
    </AccordionItem>
  );

  return (
    <div className="space-y-6">
      <p className="text-sm text-muted-foreground">{t('CONFIGURATION.ALARM_V2.START_INTRO')}</p>

      <Accordion
        type="multiple"
        value={openSections}
        onValueChange={setOpenSections}
        className="space-y-3"
      >
        {renderSection(
          'presets',
          t('CONFIGURATION.ALARM_V2.START_FROM_PRESET'),
          presetCandidates,
          t('COMMON.NO_DATA'),
        )}
        {renderSection(
          'templates',
          t('CONFIGURATION.ALARM_V2.START_FROM_TEMPLATE'),
          templateCandidates,
          t('COMMON.NO_DATA'),
        )}
        {renderSection(
          'rules',
          t('CONFIGURATION.ALARM_V2.START_FROM_RULES'),
          ruleCandidates,
          t('CONFIGURATION.ALARM_V2.NO_STANDALONE_RULES'),
        )}
      </Accordion>

      <p className="text-xs text-muted-foreground">
        {t('CONFIGURATION.ALARM_V2.START_SELECTION_HINT')}
      </p>

      <div className="mt-4 flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          {t('COMMON.CANCEL')}
        </Button>
        <Button type="button" onClick={handleContinue}>
          {selectedItemCount
            ? t('CONFIGURATION.ALARM_V2.START_WITH_RULES', { count: selectedItemCount })
            : t('CONFIGURATION.ALARM_V2.START_EMPTY')}
        </Button>
      </div>
    </div>
  );
};
