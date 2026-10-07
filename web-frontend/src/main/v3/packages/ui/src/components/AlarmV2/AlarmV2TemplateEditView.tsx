import React from 'react';
import { useTranslation } from 'react-i18next';
import { MdOutlineAdd, MdOutlineContentCopy, MdOutlineDelete } from 'react-icons/md';
import { RxChevronRight } from 'react-icons/rx';
import { AlarmV2Channel, AlarmV2Rule, AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { LoadingButton } from '@pinpoint-fe/ui/src/components/Button';
import { Input } from '@pinpoint-fe/ui/src/components/ui/input';
import { Checkbox } from '@pinpoint-fe/ui/src/components/ui/checkbox';
import { Badge } from '@pinpoint-fe/ui/src/components/ui/badge';
import { Separator } from '@pinpoint-fe/ui/src/components/ui/separator';
import { Skeleton } from '@pinpoint-fe/ui/src/components/ui/skeleton';
import { cn } from '@pinpoint-fe/ui/src/lib';
import { useAlarmV2DataSourcesQuery } from '@pinpoint-fe/ui/src/hooks/api';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@pinpoint-fe/ui/src/components/ui/accordion';
import { AlarmV2RuleForm, type FormValues, type AlarmV2RuleFormHandle } from './AlarmV2RuleForm';
import { AlarmV2SeverityBadge } from './AlarmV2StateBadge';
import { DEFAULT_ACTION_INTERVAL_SEC, DEFAULT_CHECK_INTERVAL_SEC } from './formatInterval';
import { ChannelMethodIcon } from './ChannelMethodIcon';

/** A bundle item being edited; `key` keeps React identity stable across reorders. */
interface ItemDraft {
  key: string;
  item: AlarmV2Template.TemplateItemData;
}

let draftSeq = 0;
const nextKey = () => {
  draftSeq += 1;
  return `draft-${draftSeq}`;
};

/**
 * The form takes rule data, so pick the fields it reads instead of spreading the
 * item over it. usedRuleCount is one of them: the form locks the data source once
 * rules have been stamped from the item, because changing it then is rejected by
 * the server and takes the whole bundle save down with it.
 */
const toRuleFormData = (item: AlarmV2Template.TemplateItemData): Partial<AlarmV2Rule.RuleData> => ({
  name: item.name,
  description: item.description,
  severity: item.severity,
  dataSource: item.dataSource,
  checkIntervalSec: item.checkIntervalSec,
  actionIntervalSec: item.actionIntervalSec,
  conditions: item.conditions,
  filters: item.filters,
  usedRuleCount: item.usedRuleCount,
  enabled: true,
});

// Name starts blank, and stays required: the form rejects an empty one rather
// than falling back to anything, so prefilling it would only smuggle a
// placeholder into a real alarm. The data source and metric start blank for the
// same reason -- which ones exist depends on the modules installed, so the item
// form fills them from the catalog once it arrives.
const emptyItem = (): AlarmV2Template.TemplateItemData => ({
  name: '',
  severity: 'WARNING',
  dataSource: '',
  checkIntervalSec: DEFAULT_CHECK_INTERVAL_SEC,
  actionIntervalSec: DEFAULT_ACTION_INTERVAL_SEC,
  conditions: {
    type: AlarmV2Rule.ConditionType.LEAF,
    metric: '',
    op: '>=',
    threshold: 100,
    windowSec: 3600,
  },
  filters: [],
});

export interface AlarmV2TemplateEditViewProps {
  template?: AlarmV2Template.TemplateData;
  /** What to save as the name when the viewer leaves the field empty. */
  defaultName?: string;
  channels?: AlarmV2Channel.ChannelData[];
  linkedChannelIds?: number[];
  isChannelsLoading?: boolean;
  pending?: boolean;
  disabled?: boolean;
  onSubmit: (
    values: Pick<AlarmV2Template.TemplateData, 'name' | 'description' | 'items'>,
    selectedChannelIds: number[],
  ) => void;
  onCancel: () => void;
}

export const AlarmV2TemplateEditView = ({
  template,
  defaultName,
  channels,
  linkedChannelIds,
  isChannelsLoading,
  pending,
  disabled,
  onSubmit,
  onCancel,
}: AlarmV2TemplateEditViewProps) => {
  const { t } = useTranslation();
  const [name, setName] = React.useState(template?.name ?? '');
  const [description, setDescription] = React.useState(template?.description ?? '');
  const [nameError, setNameError] = React.useState(false);
  const nameInputRef = React.useRef<HTMLInputElement>(null);
  const cardRefs = React.useRef(new Map<string, HTMLDivElement>());
  // Set when a card fails validation. The AccordionItem is mounted even when
  // collapsed, so it can be scrolled to either way -- but its fields are inside a
  // forceMount content wrapper that is display:none while closed, and a hidden
  // input cannot take focus. Opening is a state update, so the focus call waits
  // for the effect below rather than running inside the save handler.
  const [pendingScrollKey, setPendingScrollKey] = React.useState<string | null>(null);
  const [drafts, setDrafts] = React.useState<ItemDraft[]>(() =>
    (template?.items?.length ? template.items : [emptyItem()]).map((item) => ({
      key: nextKey(),
      item,
    })),
  );
  const [selectedChannelIds, setSelectedChannelIds] = React.useState<number[]>([]);
  // Every rule starts expanded: the editor exists to show what the bundle contains.
  const [openKeys, setOpenKeys] = React.useState<string[]>(() => drafts.map((draft) => draft.key));
  const handleRefs = React.useRef(new Map<string, AlarmV2RuleFormHandle | null>());
  const { data: catalog } = useAlarmV2DataSourcesQuery();
  // A card's data source lives in its own form; this follows it so the others can be limited.
  const [itemDataSources, setItemDataSources] = React.useState<Record<string, string>>({});

  // A bundle applies to applications of one category. The first card picks it and the rest
  // follow; limiting every card by the others would lock two fresh cards onto their defaults.
  const anchor = drafts[0];
  const anchorCategory = catalog?.find(
    (ds) => ds.value === (itemDataSources[anchor?.key] ?? anchor?.item.dataSource),
  )?.category;

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

  const addItem = () => {
    const draft = { key: nextKey(), item: emptyItem() };
    setDrafts((prev) => [...prev, draft]);
    setOpenKeys((prev) => [...prev, draft.key]);
  };

  const duplicateItem = (key: string) => {
    // Read the form before touching state: a card's edits live in its own form and
    // `drafts` only holds what it was mounted with, so copying the draft would
    // silently duplicate the pre-edit values.
    const edited = handleRefs.current.get(key)?.getValues();
    setDrafts((prev) => {
      const index = prev.findIndex((draft) => draft.key === key);
      if (index < 0) {
        return prev;
      }
      const source = edited ? toItem(prev[index].item, edited) : prev[index].item;
      // A copy is a new item: dropping the id makes the save an insert, so the
      // original keeps the rules already stamped from it. The usage counts belong
      // to the original too -- carried over they would lock the copy's data source.
      const copy: AlarmV2Template.TemplateItemData = {
        ...structuredClone(source),
        id: undefined,
        usedRuleCount: undefined,
        enabledUsedRuleCount: undefined,
        name: t('CONFIGURATION.ALARM_V2.COPY_OF_NAME', { name: source.name }),
      };
      const next = [...prev];
      next.splice(index + 1, 0, { key: nextKey(), item: copy });
      return next;
    });
  };

  const removeItem = (key: string) => {
    handleRefs.current.delete(key);
    setDrafts((prev) => prev.filter((draft) => draft.key !== key));
  };

  React.useEffect(() => {
    if (!pendingScrollKey) {
      return;
    }
    cardRefs.current.get(pendingScrollKey)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    handleRefs.current.get(pendingScrollKey)?.focusFirstError();
    setPendingScrollKey(null);
  }, [pendingScrollKey]);

  const handleSave = async () => {
    // The field is seeded from the source, so an empty one means the viewer cleared it.
    // Saving the name they started with beats refusing to save at all.
    const trimmedName = name.trim() || defaultName?.trim() || '';
    if (!trimmedName) {
      // Save sits at the bottom and the name at the top, so an error the user
      // cannot see reads as a dead button.
      setNameError(true);
      nameInputRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      // focus scrolls on its own by default, which lands instantly and cancels the
      // smooth scroll above -- the field ends up wherever "nearest" puts it.
      nameInputRef.current?.focus({ preventScroll: true });
      return;
    }
    setNameError(false);
    if (trimmedName !== name) {
      setName(trimmedName);
    }

    const results = await Promise.all(
      drafts.map((draft) => handleRefs.current.get(draft.key)?.validate()),
    );
    const validated: AlarmV2Template.TemplateItemData[] = [];
    const invalidKeys: string[] = [];
    for (const [i, draft] of drafts.entries()) {
      const values = results[i];
      if (!values) {
        invalidKeys.push(draft.key);
        continue;
      }
      validated.push(toItem(draft.item, values));
    }
    if (invalidKeys.length > 0) {
      // Reveal every card that failed so the messages are visible, then take the
      // user to the first one -- a card that opens off-screen is no more visible
      // than a closed one.
      setOpenKeys((prev) => Array.from(new Set([...prev, ...invalidKeys])));
      setPendingScrollKey(invalidKeys[0]);
      return;
    }

    onSubmit(
      { name: trimmedName, description: description.trim() || undefined, items: validated },
      selectedChannelIds,
    );
  };

  const channelOptions = channels?.length ? (
    <div className="flex flex-wrap gap-x-7">
      {channels.map((channel) => (
        <label
          key={channel.id}
          className="mt-2 flex cursor-pointer items-center gap-3 rounded-md p-2 text-sm"
        >
          <Checkbox
            checked={selectedChannelIds.includes(channel.id!)}
            disabled={disabled}
            onCheckedChange={() => toggleChannel(channel.id!)}
          />
          <span className="flex items-center gap-1.5">
            <span className="text-muted-foreground">
              <ChannelMethodIcon type={channel.methodType} />
            </span>
            <span className="font-medium">{channel.channelName}</span>
            <span className="text-muted-foreground">({channel.methodType})</span>
          </span>
        </label>
      ))}
    </div>
  ) : (
    <p className="text-xs text-muted-foreground">
      {t('CONFIGURATION.ALARM_V2.NO_CHANNELS_AVAILABLE')}
    </p>
  );

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-3 gap-4">
        <div className="space-y-2">
          <label className="text-sm font-medium" htmlFor="alarm-template-name">
            {t('CONFIGURATION.COMMON.NAME')}
          </label>
          <Input
            id="alarm-template-name"
            ref={nameInputRef}
            value={name}
            disabled={disabled}
            aria-invalid={nameError}
            className={nameError ? 'border-destructive' : undefined}
            placeholder={t('CONFIGURATION.ALARM_V2.TEMPLATE_NAME_PLACEHOLDER')}
            onChange={({ currentTarget }) => setName(currentTarget.value)}
          />
          {nameError && (
            <p className="text-sm font-medium text-destructive">
              {t('CONFIGURATION.ALARM_V2.VALIDATION_NAME_REQUIRED')}
            </p>
          )}
        </div>
        <div className="col-span-2 space-y-2">
          <label className="text-sm font-medium" htmlFor="alarm-template-description">
            {t('CONFIGURATION.ALARM_V2.DESCRIPTION')}{' '}
            <span className="font-normal text-muted-foreground">
              {t('CONFIGURATION.ALARM_V2.OPTIONAL')}
            </span>
          </label>
          <Input
            id="alarm-template-description"
            value={description}
            disabled={disabled}
            placeholder={t('CONFIGURATION.ALARM_V2.DESCRIPTION_PLACEHOLDER')}
            onChange={({ currentTarget }) => setDescription(currentTarget.value)}
          />
        </div>
      </div>

      <div className="space-y-3">
        <h4 className="text-sm font-semibold">{t('CONFIGURATION.ALARM_V2.LINKED_CHANNELS')}</h4>
        <p className="text-xs text-muted-foreground">
          {t('CONFIGURATION.ALARM_V2.TEMPLATE_CHANNELS_DESC')}
        </p>
        {isChannelsLoading ? (
          <div className="flex flex-wrap gap-4">
            {[0, 1, 2].map((index) => (
              <Skeleton key={index} className="h-5 w-40" />
            ))}
          </div>
        ) : (
          channelOptions
        )}
      </div>

      <Separator />

      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <h4 className="text-sm font-semibold">{t('CONFIGURATION.ALARM_V2.RULES')}</h4>
          <span className="text-sm text-muted-foreground">({drafts.length})</span>
        </div>

        {/* Collapsing hides the form, so fold its values into the draft first: the
            header reads the draft and would otherwise show pre-edit text. */}
        <Accordion
          type="multiple"
          value={openKeys}
          onValueChange={(next) => {
            const closed = openKeys.filter((key) => !next.includes(key));
            if (closed.length) {
              setDrafts((prev) =>
                prev.map((draft) => {
                  if (!closed.includes(draft.key)) {
                    return draft;
                  }
                  const edited = handleRefs.current.get(draft.key)?.getValues();
                  return edited ? { ...draft, item: toItem(draft.item, edited) } : draft;
                }),
              );
            }
            setOpenKeys(next);
          }}
          className="space-y-2"
        >
          {drafts.map((draft, index) => (
            <AccordionItem
              key={draft.key}
              value={draft.key}
              ref={(node) => {
                if (node) {
                  cardRefs.current.set(draft.key, node);
                } else {
                  cardRefs.current.delete(draft.key);
                }
              }}
              className="overflow-hidden rounded-lg border"
            >
              <div className="flex items-center gap-2 bg-muted/40 px-3 py-2.5 [&>h3]:min-w-0 [&>h3]:flex-1">
                <AccordionTrigger className="w-full py-0 hover:no-underline [&>svg]:hidden">
                  <span className="flex min-w-0 items-center gap-2">
                    <RxChevronRight
                      className={cn(
                        'shrink-0 text-muted-foreground transition-transform',
                        openKeys.includes(draft.key) && 'rotate-90',
                      )}
                    />
                    <span className="truncate text-sm font-medium">
                      {draft.item.name || t('CONFIGURATION.ALARM_V2.NEW_RULE_NAME')}
                    </span>
                    <AlarmV2SeverityBadge severity={draft.item.severity} />
                    {!openKeys.includes(draft.key) && (
                      <Badge
                        variant="outline"
                        className="border-slate-300 bg-slate-50 font-normal text-slate-600"
                      >
                        {draft.item.dataSource}
                      </Badge>
                    )}
                  </span>
                </AccordionTrigger>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7"
                  disabled={disabled}
                  aria-label={t('COMMON.DUPLICATE')}
                  onClick={() => duplicateItem(draft.key)}
                >
                  <MdOutlineContentCopy className="h-4 w-4 text-muted-foreground" />
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7"
                  disabled={disabled || drafts.length <= 1}
                  aria-label={t('COMMON.DELETE')}
                  onClick={() => removeItem(draft.key)}
                >
                  <MdOutlineDelete className="h-4 w-4 text-muted-foreground" />
                </Button>
              </div>
              {/* Kept mounted while collapsed: the rule form only holds its edits in its
                  own state, so unmounting a collapsed card would throw them away and
                  drop its validate handle from handleRefs. */}
              <AccordionContent
                forceMount
                contentWrapperClassName="data-[state=closed]:hidden"
                className="border-t px-3.5 pb-4 pt-3.5"
              >
                <AlarmV2RuleForm
                  mode="templateItem"
                  data={toRuleFormData(draft.item)}
                  dataSourceCategory={index === 0 ? undefined : anchorCategory}
                  onDataSourceChange={(dataSource) =>
                    setItemDataSources((prev) =>
                      prev[draft.key] === dataSource ? prev : { ...prev, [draft.key]: dataSource },
                    )
                  }
                  formHandleRef={(handle) => {
                    if (handle) {
                      handleRefs.current.set(draft.key, handle);
                    } else {
                      handleRefs.current.delete(draft.key);
                    }
                  }}
                />
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>

        <Button
          type="button"
          variant="outline"
          className="h-10 w-full border-dashed border-slate-300 bg-muted/20 font-medium text-muted-foreground"
          disabled={disabled}
          onClick={addItem}
        >
          <MdOutlineAdd className="mr-1" />
          {t('CONFIGURATION.ALARM_V2.ADD_RULE')}
        </Button>
      </div>

      <div className="mt-4 flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={pending}>
          {t('COMMON.CANCEL')}
        </Button>
        <LoadingButton type="button" pending={pending} disabled={disabled} onClick={handleSave}>
          {t('COMMON.SAVE')}
        </LoadingButton>
      </div>
    </div>
  );
};

// The base goes first: the fields the form does not handle (usedRuleCount, enabledUsedRuleCount,
// templateId, updatedAt) must survive, or folding a card would unlock its data source.
const toItem = (
  base: AlarmV2Template.TemplateItemData,
  values: FormValues,
): AlarmV2Template.TemplateItemData => ({
  ...base,
  name: values.name,
  description: values.description?.trim() || undefined,
  severity: values.severity,
  dataSource: values.dataSource,
  checkIntervalSec: values.checkIntervalSec,
  actionIntervalSec: values.actionIntervalSec,
  conditions: values.conditions,
  filters: values.filters ?? [],
});
