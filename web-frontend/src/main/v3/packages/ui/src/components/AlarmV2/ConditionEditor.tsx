import { useTranslation } from 'react-i18next';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import {
  Button,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Input,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@pinpoint-fe/ui/src/components/ui';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';
import { MdOutlineAdd } from 'react-icons/md';
import { formatIntervalSec } from './formatInterval';
import { applyMetric, makeDefaultLeaf, MetricOption } from './metricOption';

interface ConditionEditorProps {
  value: AlarmV2Rule.AlarmCondition;
  onChange: (value: AlarmV2Rule.AlarmCondition) => void;
  availableMetrics: MetricOption[];
  onRemove?: () => void;
  disabled?: boolean;
}

// Keep these limits aligned with backend AlarmValidationConstants: depth 2, 3 leaves per group,
// 2 root subgroups, and 10 total nodes.
const MAX_DEPTH = 2;
const MAX_LEAVES = 3;
const MAX_GROUPS = 2;

const canAddGroup = (depth: number) => depth + 2 <= MAX_DEPTH;

const WINDOW_SEC_OPTIONS = [60, 300, 600, 900, 1800, 3600, 7200, 14400, 86400];

const makeLeaf = (metrics: MetricOption[]) => makeDefaultLeaf(metrics[0]);

const makeGroup = (metrics: MetricOption[]): AlarmV2Rule.AlarmCondition => ({
  type: AlarmV2Rule.ConditionType.GROUP,
  operator: 'OR',
  criteria: [makeLeaf(metrics)],
});

const isGroup = (node: AlarmV2Rule.AlarmCondition) => node.type === AlarmV2Rule.ConditionType.GROUP;

// The tree reads left to right: a group's operator sits on the left and its
// children are bracketed to its right, so nesting is visible without indentation
// stacking downwards.

// Class strings must stay literal: Tailwind scans source text, so a composed
// name like `after:${color}` would never be generated.

/** Elbow from a group operator to its children column. */
const OP_LINK =
  "relative mr-4 after:absolute after:-right-4 after:top-1/2 after:h-0.5 after:w-4 after:bg-slate-300 after:content-['']";

/**
 * One child row: a horizontal stub to the row plus the vertical spine shared by
 * the sibling column. First and last rows only draw half the spine so the
 * bracket ends at the outermost rows.
 */
const CHILD_ROW =
  "relative flex items-center pl-[22px] before:absolute before:left-0 before:top-1/2 before:h-0.5 before:w-[22px] before:bg-slate-300 before:content-[''] after:absolute after:bottom-0 after:left-0 after:top-0 after:w-0.5 after:bg-slate-300 after:content-[''] first:after:top-1/2 last:after:bottom-1/2";

/**
 * Rows that hold a leaf (not a subgroup) at the root level stretch to the column
 * where a sibling subgroup's leaves start — 22 + 72 (operator) + 16 (elbow) + 22 —
 * so the tree does not look ragged.
 */
const DEEP_ROW = 'pl-[132px] before:w-[132px]';

const OperatorSelect = ({
  value,
  onChange,
  tooltip,
  disabled,
}: {
  value: 'AND' | 'OR';
  onChange: (v: 'AND' | 'OR') => void;
  tooltip: string;
  disabled?: boolean;
}) => (
  <Tooltip>
    <TooltipTrigger asChild>
      <span>
        <Select
          value={value}
          onValueChange={(v) => onChange(v as 'AND' | 'OR')}
          disabled={disabled}
        >
          <SelectTrigger
            aria-label={value}
            className="h-auto w-[72px] justify-between rounded-md border-slate-300 bg-slate-50 px-2.5 py-1 text-[11px] font-bold text-slate-600"
          >
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="AND" className="text-xs font-bold">
              AND
            </SelectItem>
            <SelectItem value="OR" className="text-xs font-bold">
              OR
            </SelectItem>
          </SelectContent>
        </Select>
      </span>
    </TooltipTrigger>
    <TooltipContent side="top" className="max-w-52 text-xs leading-relaxed">
      {tooltip}
    </TooltipContent>
  </Tooltip>
);

const RemoveButton = ({ onClick, disabled }: { onClick: () => void; disabled?: boolean }) => {
  const { t } = useTranslation();
  return (
    <Button
      type="button"
      variant="ghost"
      size="icon"
      aria-label={t('COMMON.DELETE')}
      className="h-7 w-7 shrink-0 text-muted-foreground hover:text-destructive"
      onClick={onClick}
      disabled={disabled}
    >
      <span aria-hidden="true" className="text-[15px] leading-none">
        −
      </span>
    </Button>
  );
};

const LeafRow = ({
  value,
  onChange,
  availableMetrics,
  onRemove,
  disabled,
}: {
  value: AlarmV2Rule.AlarmCondition;
  onChange: (value: AlarmV2Rule.AlarmCondition) => void;
  availableMetrics: MetricOption[];
  onRemove?: () => void;
  disabled?: boolean;
}) => {
  const { t } = useTranslation();
  const isNewGroup = value.trigger === 'NEW_GROUP';

  const metricOption = availableMetrics.find((m) => m.value === value.metric);
  const aggregations = metricOption?.allowedAggregations ?? [];

  const handleMetricChange = (metric: string) =>
    onChange(
      applyMetric(
        { ...value, metric },
        availableMetrics.find((m) => m.value === metric),
      ),
    );

  return (
    <div className="flex items-center gap-1.5 rounded-md border bg-background px-2.5 py-[7px]">
      <Select value={value.metric} onValueChange={handleMetricChange} disabled={disabled}>
        <SelectTrigger
          aria-label={t('CONFIGURATION.ALARM_V2.METRIC')}
          title={t('CONFIGURATION.ALARM_V2.TOOLTIP_METRIC')}
          className="h-8 w-[170px] px-2.5 text-xs"
        >
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {availableMetrics.map((m) => (
            <SelectItem key={m.value} value={m.value} className="text-xs">
              {m.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {aggregations.length > 1 && (
        <Select
          value={value.aggregation}
          onValueChange={(aggregation) => onChange({ ...value, aggregation })}
          disabled={disabled}
        >
          <SelectTrigger
            aria-label={t('CONFIGURATION.ALARM_V2.AGGREGATION')}
            title={t('CONFIGURATION.ALARM_V2.TOOLTIP_AGGREGATION')}
            className="h-8 w-[72px] px-2.5 text-xs"
          >
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {aggregations.map((aggregation) => (
              <SelectItem key={aggregation} value={aggregation} className="text-xs">
                {aggregation}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      )}

      {isNewGroup ? (
        <span className="px-1 text-xs text-muted-foreground">
          {t('CONFIGURATION.ALARM_V2.NEW_GROUP_DESC')}
        </span>
      ) : (
        <>
          <Select
            value={value.op}
            onValueChange={(op) => onChange({ ...value, op })}
            disabled={disabled}
          >
            <SelectTrigger
              aria-label={t('CONFIGURATION.ALARM_V2.OPERATOR')}
              title={t('CONFIGURATION.ALARM_V2.TOOLTIP_OPERATOR')}
              className="h-8 w-[62px] px-2.5 text-xs"
            >
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value=">">{'>'}</SelectItem>
              <SelectItem value=">=">{'>='}</SelectItem>
              <SelectItem value="<">{'<'}</SelectItem>
              <SelectItem value="<=">{'<='}</SelectItem>
            </SelectContent>
          </Select>

          <Input
            type="number"
            aria-label={t('CONFIGURATION.ALARM_V2.THRESHOLD')}
            title={t('CONFIGURATION.ALARM_V2.TOOLTIP_THRESHOLD')}
            className="h-8 w-[88px] px-2.5 text-xs"
            value={value.threshold ?? ''}
            // An emptied number input reads back as NaN, which the schema rejects with a
            // type error instead of the "required" issue the empty case deserves.
            onChange={(e) => {
              const next = e.target.valueAsNumber;
              onChange({ ...value, threshold: Number.isNaN(next) ? undefined : next });
            }}
            disabled={disabled}
          />

          {/* Reads as one sentence, the way the list summarises it: "count >= 100 in 5 min". */}
          <span className="px-0.5 text-xs text-muted-foreground">
            {t('CONFIGURATION.ALARM_V2.CONDITION_IN_LABEL')}
          </span>

          <Select
            value={String(value.windowSec ?? 3600)}
            onValueChange={(v) => onChange({ ...value, windowSec: Number(v) })}
            disabled={disabled}
          >
            <SelectTrigger
              aria-label={t('CONFIGURATION.ALARM_V2.WINDOW_SEC')}
              title={t('CONFIGURATION.ALARM_V2.TOOLTIP_WINDOW_SEC')}
              className="h-8 w-[112px] px-2.5 text-xs"
            >
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {WINDOW_SEC_OPTIONS.map((sec) => (
                <SelectItem key={sec} value={String(sec)} className="text-xs">
                  {formatIntervalSec(sec, t)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </>
      )}

      {onRemove && <RemoveButton onClick={onRemove} disabled={disabled} />}
    </div>
  );
};

const AddButtons = ({
  canAddCondition,
  canAddSubGroup,
  onAddCondition,
  onAddGroup,
  disabled,
}: {
  canAddCondition: boolean;
  canAddSubGroup: boolean;
  onAddCondition: () => void;
  onAddGroup: () => void;
  disabled?: boolean;
}) => {
  const { t } = useTranslation();
  return (
    <div className="flex gap-1.5">
      {canAddCondition && (
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="h-7 text-xs"
          onClick={onAddCondition}
          disabled={disabled}
        >
          <MdOutlineAdd className="mr-1 h-3 w-3" />
          {t('CONFIGURATION.ALARM_V2.ADD_CONDITION')}
        </Button>
      )}
      {canAddSubGroup && (
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="h-7 text-xs"
          onClick={onAddGroup}
          disabled={disabled}
        >
          <MdOutlineAdd className="mr-1 h-3 w-3" />
          {t('CONFIGURATION.ALARM_V2.ADD_GROUP')}
        </Button>
      )}
    </div>
  );
};

/** Depth is internal: only the recursion sets it, so it is not on the public props. */
interface ConditionNodeProps extends ConditionEditorProps {
  depth: number;
}

const ConditionNode = ({
  value,
  onChange,
  depth,
  availableMetrics,
  onRemove,
  disabled,
}: ConditionNodeProps) => {
  const { t } = useTranslation();

  if (value.type === AlarmV2Rule.ConditionType.LEAF) {
    // A lone leaf is still the whole condition; growing it turns it into a group.
    return (
      <div className="flex flex-col gap-2.5">
        <LeafRow
          value={value}
          onChange={onChange}
          availableMetrics={availableMetrics}
          onRemove={onRemove}
          disabled={disabled}
        />
        {depth === 0 && (
          <AddButtons
            canAddCondition
            canAddSubGroup={canAddGroup(depth)}
            disabled={disabled}
            onAddCondition={() =>
              onChange({
                type: AlarmV2Rule.ConditionType.GROUP,
                operator: 'AND',
                criteria: [value, makeLeaf(availableMetrics)],
              })
            }
            onAddGroup={() =>
              onChange({
                type: AlarmV2Rule.ConditionType.GROUP,
                operator: 'AND',
                criteria: [value, makeGroup(availableMetrics)],
              })
            }
          />
        )}
      </div>
    );
  }

  const criteria = value.criteria ?? [];
  const leafCount = criteria.filter((c) => !isGroup(c)).length;
  const groupCount = criteria.filter(isGroup).length;
  // Only the root needs the extra stretch; deeper groups hold no subgroups.
  const stretchLeafRows = depth === 0;

  const handleUpdate = (index: number, child: AlarmV2Rule.AlarmCondition) =>
    onChange({ ...value, criteria: criteria.map((c, i) => (i === index ? child : c)) });

  const handleRemove = (index: number) => {
    const next = criteria.filter((_, i) => i !== index);
    if (next.length === 0) {
      onRemove?.();
    } else if (next.length === 1 && depth === 0) {
      onChange(next[0]);
    } else {
      onChange({ ...value, criteria: next });
    }
  };

  const canAddCondition = leafCount < MAX_LEAVES;
  const canAddSubGroup = canAddGroup(depth) && groupCount < MAX_GROUPS;

  return (
    <div className="flex items-center">
      <span className={OP_LINK}>
        <OperatorSelect
          value={value.operator ?? 'AND'}
          onChange={(operator) => onChange({ ...value, operator })}
          tooltip={t('CONFIGURATION.ALARM_V2.TOOLTIP_GROUP_OPERATOR')}
          disabled={disabled}
        />
      </span>

      <div className="flex flex-col gap-2.5">
        {criteria.map((child, i) => (
          <div key={i} className={cn(CHILD_ROW, stretchLeafRows && !isGroup(child) && DEEP_ROW)}>
            <ConditionNode
              value={child}
              onChange={(c) => handleUpdate(i, c)}
              depth={depth + 1}
              availableMetrics={availableMetrics}
              onRemove={() => handleRemove(i)}
              disabled={disabled}
            />
          </div>
        ))}

        {(canAddCondition || canAddSubGroup) && (
          <div className={cn(CHILD_ROW, stretchLeafRows && DEEP_ROW)}>
            <AddButtons
              canAddCondition={canAddCondition}
              canAddSubGroup={canAddSubGroup}
              disabled={disabled}
              onAddCondition={() =>
                onChange({ ...value, criteria: [...criteria, makeLeaf(availableMetrics)] })
              }
              onAddGroup={() =>
                onChange({ ...value, criteria: [...criteria, makeGroup(availableMetrics)] })
              }
            />
          </div>
        )}
      </div>
    </div>
  );
};

export function ConditionEditor(props: ConditionEditorProps) {
  const node = <ConditionNode depth={0} {...props} />;
  // Auto margins rather than justify-center: once the tree is wider than the canvas they
  // collapse to 0, while a centered flex line pushes the left half out of scroll reach.
  return (
    <div className="overflow-x-auto rounded-md border bg-muted/25 p-3.5">
      <div className="mx-auto w-fit">{node}</div>
    </div>
  );
}
