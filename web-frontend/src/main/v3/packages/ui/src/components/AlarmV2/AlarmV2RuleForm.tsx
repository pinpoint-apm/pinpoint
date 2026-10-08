import React from 'react';
import { useTranslation } from 'react-i18next';
import { TFunction } from 'i18next';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { AlarmV2Rule, AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Input,
  Textarea,
  Button,
  Separator,
  Checkbox,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@pinpoint-fe/ui/src/components/ui';
import { Skeleton } from '@pinpoint-fe/ui/src/components/ui/skeleton';
import { LoadingButton } from '@pinpoint-fe/ui';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';
import { MdHelpOutline, MdOutlineAdd, MdOutlineRemove } from 'react-icons/md';
import { ConditionEditor } from './ConditionEditor';
import { getDefaultAggregation, makeDefaultLeaf, MetricOption } from './metricOption';
import {
  ACTION_INTERVAL_SEC_OPTIONS,
  CHECK_INTERVAL_SEC_OPTIONS,
  DEFAULT_ACTION_INTERVAL_SEC,
  DEFAULT_CHECK_INTERVAL_SEC,
  formatIntervalSec,
  roundUpActionIntervalSec,
  roundUpCheckIntervalSec,
} from './formatInterval';
import { ChannelMethodIcon } from './ChannelMethodIcon';
import { AlarmV2NoRecipientIcon } from './AlarmV2NoRecipientIcon';
import { conditionMetrics } from './formatCondition';
import { useReactToastifyToast } from '@pinpoint-fe/ui/src/components/Toast';
import { useAlarmV2DataSourcesQuery, useAlarmV2MetricsQuery } from '@pinpoint-fe/ui/src/hooks/api';

const MAX_FILTER_COUNT = 5;
const MAX_FILTER_VALUE_LENGTH = 512;
const MAX_CONDITION_NODE_COUNT = 10;
/** Mirrors AlarmValidationConstants.MAX_WINDOW_SEC on the server, which is the authority. */
const MAX_WINDOW_SEC = 86400;
const FILTER_OPERATORS = ['EQ', 'NEQ', 'CONTAINS', 'NOT_CONTAINS'] as const;
const CONDITION_OPERATORS = ['AND', 'OR'] as const;
const CONDITION_OPS = ['>', '>=', '<', '<=', '=', '=='] as const;
const CONDITION_TRIGGERS = ['NEW_GROUP', 'STATUS_CHANGE', 'PRIORITY_CHANGE'] as const;
const CONDITION_AGGREGATIONS = ['COUNT', 'SUM', 'AVG', 'MAX', 'MIN', 'P95', 'P99', 'RATE'] as const;
const THRESHOLD_TYPES = ['ABSOLUTE', 'BASELINE_CHANGE'] as const;
const EMPTY_METRICS: MetricOption[] = [];
const OVERRIDE_KEYS = [
  'name',
  'description',
  'severity',
  'checkIntervalSec',
  'actionIntervalSec',
  'conditions',
  'filters',
] as const;
type OverrideKey = (typeof OVERRIDE_KEYS)[number];

// Module scope on purpose. Declared inside the form it would be a new component type on
// every render, and the form rerenders on each keystroke, so React would tear down and
// remount all seven checkboxes -- taking focus with them mid-edit.
const OverrideControl = ({
  checked,
  onCheckedChange,
  label,
}: {
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  label: string;
}) => (
  <span className="ml-auto flex items-center gap-1 text-xs font-normal text-muted-foreground">
    <Checkbox checked={checked} onCheckedChange={(next) => onCheckedChange(next === true)} />
    <span>{label}</span>
  </span>
);

const countConditionNodes = (condition: AlarmV2Rule.AlarmCondition): number =>
  1 + (condition.criteria ?? []).reduce((sum, child) => sum + countConditionNodes(child), 0);

const getConditionAggregation = (metric?: MetricOption, aggregation?: string) => {
  const allowedAggregations = metric?.allowedAggregations;

  if (!allowedAggregations?.length) {
    return undefined;
  }
  return aggregation && allowedAggregations.includes(aggregation)
    ? aggregation
    : getDefaultAggregation(metric);
};

const normalizeConditionAggregations = (
  condition: AlarmV2Rule.AlarmCondition,
  metrics: MetricOption[],
): AlarmV2Rule.AlarmCondition => {
  if (condition.type === AlarmV2Rule.ConditionType.GROUP) {
    return {
      ...condition,
      criteria: condition.criteria?.map((child) => normalizeConditionAggregations(child, metrics)),
    };
  }

  const metric = metrics.find((m) => m.value === condition.metric);
  const aggregation = getConditionAggregation(metric, condition.aggregation);
  const nextCondition = { ...condition };

  if (aggregation) {
    nextCondition.aggregation = aggregation;
  } else {
    delete nextCondition.aggregation;
  }

  return nextCondition;
};

// zod 4 defaults a ZodType's input to unknown, and a recursive schema needs the annotation,
// so both are pinned: with the output alone, zodResolver no longer matches FormValues.
const conditionSchema: z.ZodType<AlarmV2Rule.AlarmCondition, AlarmV2Rule.AlarmCondition> = z.lazy(
  () =>
    z
      .object({
        type: z.nativeEnum(AlarmV2Rule.ConditionType),
        metric: z.string().optional(),
        op: z.enum(CONDITION_OPS).optional(),
        threshold: z.number().finite().optional(),
        windowSec: z.number().int().optional(),
        aggregation: z.enum(CONDITION_AGGREGATIONS).optional(),
        trigger: z.enum(CONDITION_TRIGGERS).optional(),
        thresholdType: z.enum(THRESHOLD_TYPES).optional(),
        baselinePeriodSec: z.number().int().optional(),
        operator: z.enum(CONDITION_OPERATORS).optional(),
        criteria: z.array(conditionSchema).optional(),
      })
      .strict()
      .superRefine((condition, ctx) => {
        if (condition.type === AlarmV2Rule.ConditionType.GROUP) {
          if (!condition.operator) {
            ctx.addIssue({
              code: z.ZodIssueCode.custom,
              path: ['operator'],
              message: 'operator is required',
            });
          }
          if (!condition.criteria?.length) {
            ctx.addIssue({
              code: z.ZodIssueCode.custom,
              path: ['criteria'],
              message: 'criteria is required',
            });
          }
          return;
        }

        if (!condition.metric) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['metric'],
            message: 'metric is required',
          });
        }
        // A NEW_GROUP condition fires on the first occurrence, so it carries no
        // comparison at all: the editor hides the operator, threshold and window for
        // it, and requiring them here would reject a row the user cannot even fill in.
        if (condition.trigger === 'NEW_GROUP') {
          return;
        }
        if (!condition.op) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['op'],
            message: 'op is required',
          });
        }
        if (condition.threshold === undefined) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['threshold'],
            message: 'threshold is required',
          });
        }
        if ((condition.windowSec ?? 0) < 60) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['windowSec'],
            message: 'windowSec must be at least 60',
          });
        }
        // The dropdown stops at a day, but a value can still arrive from a deep link or a template.
        if ((condition.windowSec ?? 0) > MAX_WINDOW_SEC) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ['windowSec'],
            message: `windowSec must be at most ${MAX_WINDOW_SEC}`,
          });
        }
      }),
);

const makeFormSchema = (t: TFunction) => {
  const filterSchema = z
    .object({
      key: z.string().min(1, t('CONFIGURATION.ALARM_V2.VALIDATION_REQUIRED')),
      op: z.enum(FILTER_OPERATORS),
      value: z
        .string()
        .min(1, t('CONFIGURATION.ALARM_V2.VALIDATION_REQUIRED'))
        .max(
          MAX_FILTER_VALUE_LENGTH,
          t('CONFIGURATION.ALARM_V2.VALIDATION_MAX_LENGTH', { max: MAX_FILTER_VALUE_LENGTH }),
        ),
    })
    .strict();

  return z
    .object({
      name: z.string().min(1, t('CONFIGURATION.ALARM_V2.VALIDATION_NAME_REQUIRED')),
      description: z.string().optional(),
      severity: z.enum(['CRITICAL', 'WARNING']),
      // Not an enum: which data sources exist depends on the modules installed, and the
      // catalog endpoint is what reports them. Emptiness is what this can still check.
      dataSource: z.string().min(1),
      checkIntervalSec: z.number(),
      actionIntervalSec: z.number(),
      conditions: conditionSchema,
      filters: z
        .array(filterSchema)
        .max(
          MAX_FILTER_COUNT,
          t('CONFIGURATION.ALARM_V2.VALIDATION_MAX_FILTERS', { max: MAX_FILTER_COUNT }),
        )
        .optional(),
      enabled: z.boolean(),
      templateItemId: z.number().nullable().optional(),
      overrideKeys: z.array(z.enum(OVERRIDE_KEYS)).optional(),
    })
    .strict()
    .superRefine((values, ctx) => {
      if (countConditionNodes(values.conditions) > MAX_CONDITION_NODE_COUNT) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['conditions'],
          message: t('CONFIGURATION.ALARM_V2.VALIDATION_MAX_CONDITION_NODES', {
            max: MAX_CONDITION_NODE_COUNT,
          }),
        });
      }
    });
};

export type FormValues = z.infer<ReturnType<typeof makeFormSchema>>;

export interface AlarmV2RuleFormProps {
  data?: Partial<AlarmV2Rule.RuleData>;
  channels?: AlarmV2Channel.ChannelData[];
  isChannelsLoading?: boolean;
  linkedChannelIds?: number[];
  /**
   * `rule` edits an application rule, `templateItem` one rule definition inside a
   * bundle - the latter drops the channel picker and the footer because the bundle
   * page owns both, and is driven through {@link AlarmV2RuleFormHandle} instead of
   * a submit button.
   */
  mode?: 'rule' | 'templateItem';
  /** The type of the application a rule targets; only the data sources it may use are offered. */
  applicationType?: string;
  /** Limits the data sources offered to one category, for a bundle item next to others. */
  dataSourceCategory?: string;
  onDataSourceChange?: (dataSource: string) => void;
  pending?: boolean;
  formHandleRef?: React.Ref<AlarmV2RuleFormHandle>;
  onSubmit?: (values: FormValues, selectedChannelIds: number[]) => void;
  onCancel?: () => void;
}

export interface AlarmV2RuleFormHandle {
  /** Validates and returns the current values, or null when invalid. */
  validate: () => Promise<FormValues | null>;
  getValues: () => FormValues;
  /**
   * Puts the cursor on the first field that failed validation, in render order.
   *
   * The condition editor is not a registered field, so an error inside it moves no
   * cursor -- the message under the editor is what tells the user there. The caller
   * scrolls the card into view either way.
   */
  focusFirstError: () => void;
}

const HintIcon = ({ text }: { text: string }) => (
  <Tooltip>
    <TooltipTrigger asChild>
      <span className="cursor-help text-muted-foreground/40 hover:text-muted-foreground transition-colors inline-flex">
        <MdHelpOutline className="h-3 w-3" />
      </span>
    </TooltipTrigger>
    <TooltipContent side="top" className="max-w-56 text-xs leading-relaxed whitespace-pre-line">
      {text}
    </TooltipContent>
  </Tooltip>
);

const cloneFilters = (filters?: AlarmV2Rule.AlarmFilter[]) =>
  filters ? filters.map((filter) => ({ ...filter })) : [];

const toFormValues = (data?: Partial<AlarmV2Rule.RuleData>): FormValues => ({
  name: data?.name || '',
  description: data?.description || '',
  severity: data?.severity || 'WARNING',
  dataSource: data?.dataSource || '',
  checkIntervalSec: roundUpCheckIntervalSec(data?.checkIntervalSec || DEFAULT_CHECK_INTERVAL_SEC),
  actionIntervalSec: roundUpActionIntervalSec(
    data?.actionIntervalSec || DEFAULT_ACTION_INTERVAL_SEC,
  ),
  conditions: data?.conditions
    ? structuredClone(data.conditions)
    : {
        type: AlarmV2Rule.ConditionType.LEAF,
        metric: 'error_count',
        op: '>=',
        threshold: 100,
        windowSec: 3600,
      },
  filters: cloneFilters(data?.filters),
  enabled: data?.enabled ?? true,
  templateItemId: data?.templateItemId ?? null,
  overrideKeys: (data?.overrideKeys ?? []) as OverrideKey[],
});

const LinkedChannels = ({
  channels,
  isLoading,
  locked,
  selectedIds,
  onToggle,
}: {
  channels?: AlarmV2Channel.ChannelData[];
  isLoading?: boolean;
  /** A rule linked to a bundle item uses the bundle's channels and cannot pick its own. */
  locked: boolean;
  selectedIds: number[];
  onToggle: (id: number) => void;
}) => {
  const { t } = useTranslation();
  const options =
    channels && channels.length > 0 ? (
      <div className="space-y-2">
        {channels.map((ch) => (
          <label
            key={ch.id}
            className={cn(
              'flex items-center gap-3 p-2 rounded-md',
              locked ? 'cursor-default' : 'cursor-pointer hover:bg-accent',
            )}
          >
            <Checkbox
              checked={selectedIds.includes(ch.id!)}
              disabled={locked}
              onCheckedChange={() => onToggle(ch.id!)}
            />
            <span className="flex items-center gap-1.5 text-sm">
              <ChannelMethodIcon type={ch.methodType} />
              <span className="font-medium">{ch.channelName}</span>
              <span className="text-muted-foreground">({ch.methodType})</span>
              <AlarmV2NoRecipientIcon channel={ch} />
            </span>
          </label>
        ))}
      </div>
    ) : (
      <p className="text-xs text-muted-foreground">
        {t(
          locked
            ? 'CONFIGURATION.ALARM_V2.NO_TEMPLATE_CHANNELS'
            : 'CONFIGURATION.ALARM_V2.NO_CHANNELS_AVAILABLE',
        )}
      </p>
    );

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-1">
        <h4 className="text-sm font-semibold">{t('CONFIGURATION.ALARM_V2.LINKED_CHANNELS')}</h4>
        <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_LINKED_CHANNELS')} />
      </div>
      <p className="text-xs text-muted-foreground">
        {t(
          locked
            ? 'CONFIGURATION.ALARM_V2.TEMPLATE_CHANNELS_USED'
            : 'CONFIGURATION.ALARM_V2.LINKED_CHANNELS_DESC',
        )}
      </p>
      {isLoading ? (
        <div className="space-y-2">
          {[0, 1, 2].map((index) => (
            <div key={index} className="flex items-center gap-3 p-2">
              <Skeleton className="h-4 w-4 rounded-sm" />
              <Skeleton className="h-4 w-48" />
            </div>
          ))}
        </div>
      ) : (
        options
      )}
    </div>
  );
};

export const AlarmV2RuleForm = ({
  data,
  channels,
  isChannelsLoading,
  linkedChannelIds,
  mode = 'rule',
  applicationType,
  dataSourceCategory,
  onDataSourceChange,
  pending,
  formHandleRef,
  onSubmit,
  onCancel,
}: AlarmV2RuleFormProps) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const isEdit = !!data?.id;
  const isItemForm = mode === 'templateItem';
  const [selectedChannelIds, setSelectedChannelIds] = React.useState<number[]>([]);

  const formSchema = React.useMemo(() => makeFormSchema(t), [t]);

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: toFormValues(data),
  });

  const watchDataSource = form.watch('dataSource');
  const watchConditions = form.watch('conditions');
  const watchTemplateItemId = form.watch('templateItemId');
  const watchOverrideKeys = form.watch('overrideKeys') ?? [];
  const selectedTemplate = data?.templateItem;
  const isTemplateLinkedRule = !isItemForm && !!watchTemplateItemId;
  const isDataSourceLocked = isTemplateLinkedRule || (isItemForm && (data?.usedRuleCount ?? 0) > 0);

  const { data: catalog, isError: isCatalogError } = useAlarmV2DataSourcesQuery(applicationType);
  const dataSources = React.useMemo(
    () =>
      dataSourceCategory ? catalog?.filter((ds) => ds.category === dataSourceCategory) : catalog,
    [catalog, dataSourceCategory],
  );

  // A category the parent narrows to later can leave the chosen one out; clearing it lets the
  // default below pick from what is offered. A data source rules read stays as it is.
  React.useEffect(() => {
    if (
      dataSourceCategory &&
      dataSources &&
      watchDataSource &&
      !isDataSourceLocked &&
      !dataSources.some((ds) => ds.value === watchDataSource)
    ) {
      form.setValue('dataSource', '');
    }
  }, [dataSourceCategory, dataSources, watchDataSource, isDataSourceLocked, form]);

  React.useEffect(() => {
    if (watchDataSource) {
      onDataSourceChange?.(watchDataSource);
    }
    // The callback is recreated by every parent render; the value is what should trigger it.
  }, [watchDataSource]); // eslint-disable-line react-hooks/exhaustive-deps
  const { data: metricsData, isError: isMetricsError } = useAlarmV2MetricsQuery(watchDataSource);

  // A new rule opens with a data source already chosen, the way it did when the list was
  // fixed. Which one that is has to wait for the catalog, so it is filled in on arrival and
  // only while the field is still untouched.
  React.useEffect(() => {
    if (!watchDataSource && dataSources?.length) {
      form.setValue('dataSource', dataSources[0].value);
    }
  }, [dataSources, watchDataSource, form]);

  const availableMetrics: MetricOption[] = metricsData ?? EMPTY_METRICS;

  /**
   * Both fields the catalog fills are empty until it answers, and the form would report that
   * emptiness as the operator's mistake. Submitting also has to wait for the metrics of the
   * data source actually selected, or it sends the one the previous selection had.
   */
  const catalogPending =
    !isCatalogError && (!dataSources || (!!watchDataSource && !metricsData && !isMetricsError));
  // From the whole catalog: a locked item outside the category offered still keeps its keys.
  const currentDataSource = catalog?.find((ds) => ds.value === watchDataSource);
  const availableFilterKeys = React.useMemo(
    () => currentDataSource?.filterKeys ?? [],
    [currentDataSource],
  );
  const hasOverride = (key: OverrideKey) => watchOverrideKeys.includes(key);

  const restoreTemplateValue = React.useCallback(
    (key: OverrideKey) => {
      if (!selectedTemplate) {
        return;
      }
      if (key === 'name') {
        form.setValue('name', selectedTemplate.name, { shouldValidate: true });
      } else if (key === 'description') {
        form.setValue('description', selectedTemplate.description ?? '', { shouldValidate: true });
      } else if (key === 'severity') {
        form.setValue('severity', selectedTemplate.severity, { shouldValidate: true });
      } else if (key === 'checkIntervalSec') {
        form.setValue(
          'checkIntervalSec',
          roundUpCheckIntervalSec(selectedTemplate.checkIntervalSec),
          {
            shouldValidate: true,
          },
        );
      } else if (key === 'actionIntervalSec') {
        form.setValue(
          'actionIntervalSec',
          roundUpActionIntervalSec(selectedTemplate.actionIntervalSec),
          { shouldValidate: true },
        );
      } else if (key === 'conditions') {
        form.setValue('conditions', structuredClone(selectedTemplate.conditions), {
          shouldValidate: true,
        });
      } else if (key === 'filters') {
        form.setValue('filters', cloneFilters(selectedTemplate.filters), { shouldValidate: true });
      }
    },
    [form, selectedTemplate],
  );

  const setOverride = (key: OverrideKey, checked: boolean) => {
    const nextKeys = checked
      ? [...new Set([...watchOverrideKeys, key])]
      : watchOverrideKeys.filter((overrideKey) => overrideKey !== key);
    form.setValue('overrideKeys', nextKeys, { shouldValidate: true });
    if (!checked) {
      restoreTemplateValue(key);
    }
  };

  const {
    fields: filterFields,
    append: appendFilter,
    remove: removeFilter,
  } = useFieldArray({
    control: form.control,
    name: 'filters',
  });
  const canAddFilter = filterFields.length < MAX_FILTER_COUNT && availableFilterKeys.length > 0;

  React.useEffect(() => {
    if (!metricsData?.length) return;
    const validMetrics = new Set(metricsData.map((m) => m.value));

    const hasInvalid = (cond: AlarmV2Rule.AlarmCondition): boolean => {
      if (cond.type === AlarmV2Rule.ConditionType.LEAF) {
        return !validMetrics.has(cond.metric ?? '');
      }
      return (cond.criteria ?? []).some(hasInvalid);
    };

    if (hasInvalid(form.getValues('conditions'))) {
      // Conditions the form filled in or that belong to another data source are replaced
      // quietly. A metric the catalog no longer reports on the saved data source is a
      // condition the user saved and is about to lose, so say so rather than dropping the
      // tree behind their back.
      const savedMetrics =
        data?.conditions && watchDataSource === data.dataSource
          ? new Set(conditionMetrics(data.conditions))
          : new Set<string>();
      const droppedMetrics = conditionMetrics(form.getValues('conditions')).filter(
        (metric) => !validMetrics.has(metric) && savedMetrics.has(metric),
      );
      if (droppedMetrics.length) {
        toast.warning(
          t('CONFIGURATION.ALARM_V2.CONDITION_RESET_UNSUPPORTED_METRIC', {
            metrics: droppedMetrics.join(', '),
          }),
        );
      }
      form.setValue('conditions', makeDefaultLeaf(metricsData[0]));
      return;
    }

    const conditions = form.getValues('conditions');
    const normalizedConditions = normalizeConditionAggregations(conditions, metricsData);
    if (JSON.stringify(normalizedConditions) !== JSON.stringify(conditions)) {
      form.setValue('conditions', normalizedConditions, { shouldValidate: true });
    }
    // t and toast are left out on purpose: useReactToastifyToast builds a fresh object every
    // render, so depending on it would rerun this effect on each one and fire the warning
    // over and over. The catalog is what should trigger it.
  }, [metricsData, form]); // eslint-disable-line react-hooks/exhaustive-deps

  React.useEffect(() => {
    if (!currentDataSource) return;
    const filters = form.getValues('filters') ?? [];
    const nextFilters = filters
      .filter((filter) => availableFilterKeys.includes(filter.key))
      .slice(0, MAX_FILTER_COUNT);

    if (nextFilters.length !== filters.length) {
      form.setValue('filters', nextFilters, { shouldValidate: true });
    }
  }, [availableFilterKeys, currentDataSource, form]);

  React.useEffect(() => {
    if (!isChannelsLoading) {
      setSelectedChannelIds(linkedChannelIds ?? []);
    }
  }, [linkedChannelIds, isChannelsLoading]);

  const toggleChannel = (channelId: number) => {
    setSelectedChannelIds((prev) =>
      prev.includes(channelId) ? prev.filter((id) => id !== channelId) : [...prev, channelId],
    );
  };

  const renderOverrideControl = (overrideKey: OverrideKey) =>
    isTemplateLinkedRule ? (
      <OverrideControl
        checked={hasOverride(overrideKey)}
        onCheckedChange={(checked) => setOverride(overrideKey, checked)}
        label={t('CONFIGURATION.ALARM_V2.OVERRIDE')}
      />
    ) : null;

  const isTemplateValueLocked = (overrideKey: OverrideKey) =>
    isTemplateLinkedRule && !hasOverride(overrideKey);

  React.useImperativeHandle(formHandleRef, () => ({
    validate: async () => ((await form.trigger()) ? form.getValues() : null),
    getValues: () => form.getValues(),
    // Walks the registered fields in render order, so nested ones (filters.0.value)
    // land too -- setFocus with the first errors key does not, since that key is the
    // group name and no input is registered under it.
    focusFirstError: () => void form.trigger(undefined, { shouldFocus: true }),
  }));

  // A bundle item card lives inside the bundle page, which has its own form.
  const FormTag = isItemForm ? 'div' : 'form';

  const nameField = (
    <FormField
      control={form.control}
      name="name"
      render={({ field, fieldState }) => (
        <FormItem>
          <FormLabel className="flex items-center gap-1">
            {t('CONFIGURATION.COMMON.NAME')}
            <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_RULE_NAME')} />
            {renderOverrideControl('name')}
          </FormLabel>
          <FormControl>
            <Input
              {...field}
              disabled={isTemplateValueLocked('name')}
              className={cn({ 'border-destructive': fieldState.invalid })}
              placeholder={t('CONFIGURATION.ALARM_V2.RULE_NAME_PLACEHOLDER')}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );

  const descriptionField = (
    <FormField
      control={form.control}
      name="description"
      render={({ field }) => (
        <FormItem>
          <FormLabel className="flex items-center gap-1">
            {t('CONFIGURATION.ALARM_V2.DESCRIPTION')}
            <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_DESCRIPTION')} />
            {renderOverrideControl('description')}
          </FormLabel>
          <FormControl>
            <Textarea
              {...field}
              disabled={isTemplateValueLocked('description')}
              placeholder={t('CONFIGURATION.ALARM_V2.DESCRIPTION_PLACEHOLDER')}
              maxLength={500}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );

  const severityField = (
    <FormField
      control={form.control}
      name="severity"
      render={({ field }) => (
        <FormItem>
          <FormLabel className="flex w-full items-center gap-1">
            {t('CONFIGURATION.ALARM_V2.SEVERITY')}
            <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_SEVERITY')} />
            {renderOverrideControl('severity')}
          </FormLabel>
          <Select
            onValueChange={field.onChange}
            value={field.value}
            disabled={isTemplateValueLocked('severity')}
          >
            <FormControl>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              <SelectItem value="CRITICAL">CRITICAL</SelectItem>
              <SelectItem value="WARNING">WARNING</SelectItem>
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );

  const dataSourceField = (
    <FormField
      control={form.control}
      name="dataSource"
      render={({ field }) => (
        <FormItem>
          <FormLabel className="flex items-center gap-1">
            {t('CONFIGURATION.ALARM_V2.DATA_SOURCE')}
            <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_DATA_SOURCE')} />
          </FormLabel>
          <Select onValueChange={field.onChange} value={field.value} disabled={isDataSourceLocked}>
            <FormControl>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {dataSources?.map((ds) => (
                <SelectItem key={ds.value} value={ds.value}>
                  {ds.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );

  const checkIntervalField = (
    <FormField
      control={form.control}
      name="checkIntervalSec"
      render={({ field }) => (
        <FormItem>
          <FormLabel className="flex w-full items-center gap-1">
            {t('CONFIGURATION.ALARM_V2.CHECK_INTERVAL')}
            <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_CHECK_INTERVAL')} />
            {renderOverrideControl('checkIntervalSec')}
          </FormLabel>
          <Select
            onValueChange={(value) => field.onChange(Number(value))}
            value={String(field.value)}
            disabled={isTemplateValueLocked('checkIntervalSec')}
          >
            <FormControl>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {CHECK_INTERVAL_SEC_OPTIONS.map((sec) => (
                <SelectItem key={sec} value={String(sec)}>
                  {formatIntervalSec(sec, t)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );

  const actionIntervalField = (
    <FormField
      control={form.control}
      name="actionIntervalSec"
      render={({ field }) => (
        <FormItem>
          <FormLabel className="flex w-full items-center gap-1">
            {t('CONFIGURATION.ALARM_V2.ACTION_INTERVAL')}
            <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_ACTION_INTERVAL')} />
            {renderOverrideControl('actionIntervalSec')}
          </FormLabel>
          <Select
            onValueChange={(value) => field.onChange(Number(value))}
            value={String(field.value)}
            disabled={isTemplateValueLocked('actionIntervalSec')}
          >
            <FormControl>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {ACTION_INTERVAL_SEC_OPTIONS.map((sec) => (
                <SelectItem key={sec} value={String(sec)}>
                  {formatIntervalSec(sec, t)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
  );

  return (
    <TooltipProvider delayDuration={300}>
      <Form {...form}>
        <FormTag
          {...(isItemForm
            ? {}
            : {
                onSubmit: form.handleSubmit((values) => onSubmit?.(values, selectedChannelIds)),
              })}
          className="space-y-4"
        >
          {isItemForm ? (
            <>
              <div className="grid grid-cols-2 gap-4">
                {nameField}
                {severityField}
              </div>
              {descriptionField}
              <div className="grid grid-cols-2 gap-4">
                {dataSourceField}
                <div className="flex gap-2">
                  <div className="flex-1">{checkIntervalField}</div>
                  <div className="flex-1">{actionIntervalField}</div>
                </div>
              </div>
            </>
          ) : (
            <>
              <div className="grid grid-cols-3 gap-4">
                <div>{nameField}</div>
                <div className="col-span-2">{descriptionField}</div>
              </div>
              <Separator />
              <LinkedChannels
                channels={channels}
                isLoading={isChannelsLoading}
                locked={isTemplateLinkedRule}
                selectedIds={selectedChannelIds}
                onToggle={toggleChannel}
              />
              <div className="grid grid-cols-2 gap-4">
                {severityField}
                {dataSourceField}
              </div>
              <div className="grid grid-cols-2 gap-4">
                {checkIntervalField}
                {actionIntervalField}
              </div>
            </>
          )}

          <Separator />
          <div className="space-y-3">
            <div className="flex items-center gap-1">
              <h4 className="text-sm font-semibold">{t('CONFIGURATION.ALARM_V2.CONDITION')}</h4>
              <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_CONDITION')} />
              {renderOverrideControl('conditions')}
            </div>
            <ConditionEditor
              value={watchConditions}
              onChange={(newVal) => form.setValue('conditions', newVal, { shouldValidate: true })}
              availableMetrics={availableMetrics}
              disabled={isTemplateValueLocked('conditions')}
            />
            {/* A missing threshold fails at conditions.threshold, so the group itself
                carries no message -- without the fallback Save would refuse silently. */}
            {form.formState.errors.conditions && (
              <p className="text-sm font-medium text-destructive">
                {form.formState.errors.conditions.message ??
                  t('CONFIGURATION.ALARM_V2.VALIDATION_CONDITION_INCOMPLETE')}
              </p>
            )}
          </div>

          <Separator />
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-1">
                <h4 className="text-sm font-semibold">{t('CONFIGURATION.ALARM_V2.FILTERS')}</h4>
                <HintIcon text={t('CONFIGURATION.ALARM_V2.TOOLTIP_FILTERS')} />
                {renderOverrideControl('filters')}
              </div>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={!canAddFilter || isTemplateValueLocked('filters')}
                onClick={() =>
                  appendFilter({ key: availableFilterKeys[0] ?? '', op: 'EQ', value: '' })
                }
              >
                <MdOutlineAdd className="mr-1" />
                {t('CONFIGURATION.ALARM_V2.ADD_FILTER')}
              </Button>
            </div>

            {filterFields.map((field, index) => (
              <div key={field.id} className="flex items-end gap-2">
                <FormField
                  control={form.control}
                  name={`filters.${index}.key`}
                  render={({ field: f }) => (
                    <FormItem className="flex-1">
                      {index === 0 && (
                        <FormLabel>{t('CONFIGURATION.ALARM_V2.FILTER_KEY')}</FormLabel>
                      )}
                      <Select
                        onValueChange={f.onChange}
                        value={f.value}
                        disabled={isTemplateValueLocked('filters')}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {availableFilterKeys.map((k) => (
                            <SelectItem key={k} value={k}>
                              {k}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name={`filters.${index}.op`}
                  render={({ field: f }) => (
                    <FormItem className="w-36">
                      {index === 0 && (
                        <FormLabel>{t('CONFIGURATION.ALARM_V2.FILTER_OP')}</FormLabel>
                      )}
                      <Select
                        onValueChange={f.onChange}
                        value={f.value}
                        disabled={isTemplateValueLocked('filters')}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {FILTER_OPERATORS.map((op) => (
                            <SelectItem key={op} value={op}>
                              {op}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name={`filters.${index}.value`}
                  render={({ field: f, fieldState }) => (
                    <FormItem className="flex-1">
                      {index === 0 && (
                        <FormLabel>{t('CONFIGURATION.ALARM_V2.FILTER_VALUE')}</FormLabel>
                      )}
                      <FormControl>
                        <Input
                          {...f}
                          className={cn({ 'border-destructive': fieldState.invalid })}
                          maxLength={MAX_FILTER_VALUE_LENGTH}
                          placeholder={t('CONFIGURATION.ALARM_V2.FILTER_VALUE')}
                          disabled={isTemplateValueLocked('filters')}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="shrink-0"
                  onClick={() => removeFilter(index)}
                  disabled={isTemplateValueLocked('filters')}
                >
                  <MdOutlineRemove />
                </Button>
              </div>
            ))}
            {form.formState.errors.filters?.message && (
              <p className="text-sm font-medium text-destructive">
                {form.formState.errors.filters.message}
              </p>
            )}
          </div>

          {!isItemForm && (
            <>
              <Separator />
              <div className="flex justify-end gap-2">
                {onCancel && (
                  <Button type="button" variant="outline" onClick={onCancel} disabled={pending}>
                    {t('COMMON.CANCEL')}
                  </Button>
                )}
                <LoadingButton
                  type="submit"
                  pending={pending || form.formState.isSubmitting || catalogPending}
                >
                  {isEdit ? t('COMMON.SAVE') : t('COMMON.ADD')}
                </LoadingButton>
              </div>
            </>
          )}
        </FormTag>
      </Form>
    </TooltipProvider>
  );
};
