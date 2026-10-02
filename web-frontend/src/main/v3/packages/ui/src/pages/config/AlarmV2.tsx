import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  AlarmV2Rule,
  AlarmV2Channel,
  AlarmV2Template,
  ApplicationType,
} from '@pinpoint-fe/ui/src/constants/types';
import { Separator } from '@pinpoint-fe/ui/src/components/ui/separator';
import { APP_SETTING_KEYS, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2RuleList } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleList';
import { AlarmV2ChannelList } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ChannelList';
import { AlarmV2TemplateList } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateList';
import { AlarmV2TemplateEditView } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateEditView';
import { AlarmV2TemplateStartView } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateStartView';
import { AlarmV2EmptyState } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2EmptyState';
import {
  AlarmV2RuleForm,
  type FormValues,
} from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleForm';
import { AlarmV2ChannelForm } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ChannelForm';
import { AlarmV2HistorySheet } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2HistorySheet';
import { AlarmV2Sheet } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2Sheet';
import { AlarmV2ConfirmDialog } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ConfirmDialog';
import { formatOverrideKey } from '@pinpoint-fe/ui/src/components/AlarmV2/formatOverrideKey';
import { buildRuleSaveData } from '@pinpoint-fe/ui/src/components/AlarmV2/buildRuleSaveData';
import {
  DEFAULT_ACTION_INTERVAL_SEC,
  DEFAULT_CHECK_INTERVAL_SEC,
} from '@pinpoint-fe/ui/src/components/AlarmV2/formatInterval';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@pinpoint-fe/ui/src/components/ui/tabs';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { Badge } from '@pinpoint-fe/ui/src/components/ui/badge';
import { Input } from '@pinpoint-fe/ui/src/components/ui/input';
import { useReactToastifyToast } from '@pinpoint-fe/ui/src/components/Toast';
import { DataTableSkeleton } from '@pinpoint-fe/ui';
import { useGetUserGroup } from '@pinpoint-fe/ui/src/hooks';
import { useLocalStorage } from 'usehooks-ts';
import {
  useAlarmV2RuleMutation,
  useAlarmV2ChannelMutation,
  useAlarmV2ChannelQuery,
  useAlarmV2DataSourcesQuery,
  useAlarmV2ChannelsByRuleQuery,
  useAlarmV2ChannelsByTemplateQuery,
  useAlarmV2RuleHistoryQuery,
  useAlarmV2RuleDetailQuery,
  useAlarmV2RuleStateQuery,
  useAlarmV2RuleQuery,
  useAlarmV2TemplateQuery,
  useAlarmV2TemplatePresetsQuery,
  useAlarmV2TemplateApplyMutation,
  useAlarmV2TemplateMutation,
} from '@pinpoint-fe/ui/src/hooks/api';
import { useRequestService } from '@pinpoint-fe/ui/src/hooks/utility/useRequestService';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { AlarmV2StateBadge } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2StateBadge';
import { MdOutlineAdd } from 'react-icons/md';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';

const AlarmV2TableSearch = ({
  value,
  placeholder,
  onChange,
}: {
  value: string;
  placeholder: string;
  onChange: (value: string) => void;
}) => {
  return (
    <div className="relative w-full sm:w-72">
      <RxMagnifyingGlass className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
      <Input
        value={value}
        aria-label={placeholder}
        placeholder={placeholder}
        className="pl-9"
        onChange={({ currentTarget }) => onChange(currentTarget.value)}
      />
    </div>
  );
};

const AlarmV2RuleSheetTitle = ({
  title,
  rule,
  status,
}: {
  title: React.ReactNode;
  rule?: Partial<AlarmV2Rule.RuleData>;
  status?: AlarmV2Rule.StateResponse['status'];
}) => {
  const { t } = useTranslation();
  const overrideKeys = rule?.overrideKeys ?? [];

  return (
    <div className="min-w-0 flex-1 space-y-2">
      <span className="flex items-center gap-2">
        {title}
        {status && <AlarmV2StateBadge status={status} />}
      </span>
      {rule?.templateItemId && (
        <div className="flex min-w-0 flex-wrap items-center gap-1.5 text-xs font-normal text-muted-foreground">
          <span>{t('CONFIGURATION.ALARM_V2.TEMPLATE')}</span>
          <Badge
            variant="outline"
            className="max-w-[12rem] truncate border-slate-300 bg-slate-50 text-slate-600"
          >
            {rule.templateName || `#${rule.templateId}`}
          </Badge>
          {rule.templateItemName && (
            <>
              <span aria-hidden="true">·</span>
              <Badge
                variant="outline"
                className="max-w-[12rem] truncate border-slate-300 bg-slate-50 text-slate-600"
              >
                {rule.templateItemName}
              </Badge>
            </>
          )}
          <span aria-hidden="true">·</span>
          <span>{t('CONFIGURATION.ALARM_V2.OVERRIDES')}</span>
          {overrideKeys.length > 0 ? (
            overrideKeys.map((key) => (
              <Badge
                key={key}
                variant="outline"
                className="border-primary/30 bg-primary/10 text-primary"
              >
                {formatOverrideKey(key, t)}
              </Badge>
            ))
          ) : (
            <span>{t('CONFIGURATION.ALARM_V2.NO_OVERRIDES')}</span>
          )}
        </div>
      )}
    </div>
  );
};

const AlarmV2FullPage = ({
  title,
  onBack,
  children,
}: {
  title: React.ReactNode;
  onBack: () => void;
  children: React.ReactNode;
}) => {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <div className="flex items-start gap-2 text-lg font-semibold">
        {/* onBack navigates itself; without preventDefault the Link would navigate a
            second time and leave two history entries to step back through. */}
        <Link
          to=""
          className="text-muted-foreground hover:underline"
          onClick={(event) => {
            event.preventDefault();
            onBack();
          }}
        >
          {t('CONFIGURATION.ALARM_V2.NAME')}
        </Link>
        <span className="text-muted-foreground">›</span>
        {title}
      </div>
      <Separator />
      {/* Below this width the two-column rows overlap; the body scrolls instead. */}
      <div className="overflow-x-auto">
        <div className="min-w-[28rem]">{children}</div>
      </div>
    </div>
  );
};

// A new bundle has nothing linked yet. Kept at module scope because the editor
// resets its channel selection whenever this prop's identity changes, and an
// inline [] would be a new array on every render.
const NO_LINKED_CHANNEL_IDS: number[] = [];

const channelIdsOf = (channels?: AlarmV2Channel.ChannelData[]) =>
  (channels || []).map((c) => c.id).filter((id): id is number => id !== undefined);

export interface AlarmV2ApplicationListProps {
  selectedApplication?: ApplicationType;
  onClickApplication: (application: ApplicationType) => void;
}

export interface AlarmV2PageProps {
  /** Whether the viewer may create, change or delete anything here. How that is decided is
   *  the deployment's business; this screen only hides what cannot be used. */
  hasPermission?: boolean;
  /** The applications a rule may target, which a deep link and the stored selection are
   *  resolved against. */
  applications?: ApplicationType[];
  ApplicationList?: (props: AlarmV2ApplicationListProps) => React.ReactElement;
  /** Where the picker's selection is remembered, so screens offering different applications
   *  keep theirs apart. */
  selectedApplicationStorageKey?: string;
}

export const AlarmV2Page = ({
  hasPermission = false,
  applications,
  ApplicationList,
  selectedApplicationStorageKey = APP_SETTING_KEYS.CONFIG_ALARM_V2_SELECTED_APPLICATION,
}: AlarmV2PageProps) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [storedApplication, setSelectedApplication] = useLocalStorage<ApplicationType | undefined>(
    selectedApplicationStorageKey,
    undefined,
  );
  /**
   * A stored selection this screen cannot offer reads as none, and stays stored. The key is
   * shared by every tab, while the service each tab is looking at is not, so clearing it here
   * would take away a selection another tab is still using -- and that write comes back as a
   * storage event, which is how a second tab emptied the one in front of the user.
   */
  const selectedApplication = React.useMemo(() => {
    if (!storedApplication || !applications) {
      return storedApplication;
    }
    return applications.some(
      (item: ApplicationType) =>
        item.applicationName === storedApplication.applicationName &&
        item.serviceType === storedApplication.serviceType,
    )
      ? storedApplication
      : undefined;
  }, [storedApplication, applications]);
  const [activeTab, setActiveTab] = React.useState('rules');
  const [currentDeletingRule, setCurrentDeletingRule] = React.useState<AlarmV2Rule.RuleData>();
  const [currentDeletingChannel, setCurrentDeletingChannel] =
    React.useState<AlarmV2Channel.ChannelData>();
  const [pendingTemplateSave, setPendingTemplateSave] = React.useState<{
    templateData: Pick<AlarmV2Template.TemplateData, 'name' | 'description' | 'items'>;
    selectedChannelIds: number[];
  }>();
  const [currentDeletingTemplate, setCurrentDeletingTemplate] =
    React.useState<AlarmV2Template.TemplateData>();
  const [currentHistoryRule, setCurrentHistoryRule] = React.useState<AlarmV2Rule.RuleData>();
  const [isApplyPickerOpen, setIsApplyPickerOpen] = React.useState(false);
  const [unlinkingTemplate, setUnlinkingTemplate] = React.useState<{
    templateId: number;
    templateName: string;
    ruleCount: number;
  }>();
  const [draftTemplateItems, setDraftTemplateItems] =
    React.useState<AlarmV2Template.TemplateItemData[]>();
  const [ruleSearchQuery, setRuleSearchQuery] = React.useState('');
  const [channelSearchQuery, setChannelSearchQuery] = React.useState('');
  const [templateSearchQuery, setTemplateSearchQuery] = React.useState('');

  // `?view=` drives the full-page views; `?ruleId=` keeps its existing meaning
  // (opening the history sheet), so the two never collide.
  const view = searchParams.get('view');
  const viewTemplateId = Number(searchParams.get('templateId')) || undefined;
  const viewRuleId = Number(searchParams.get('ruleId')) || undefined;
  const viewChannelId = Number(searchParams.get('channelId')) || undefined;
  const isTemplateNewView = view === 'template-new';
  const isTemplateEditView = view === 'template-edit' && !!viewTemplateId;
  const isRuleView = view === 'rule-new' || (view === 'rule-edit' && !!viewRuleId);
  const isChannelView = view === 'channel-new' || (view === 'channel-edit' && !!viewChannelId);
  const closeView = React.useCallback(() => {
    setDraftTemplateItems(undefined);
    navigate('');
  }, [navigate]);

  // Rules, bundles and channels all show counts derived from the other two, so every
  // write refreshes the set rather than the one list it touched.
  const invalidateAlarmQueries = React.useCallback(
    () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_RULE] }),
        queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_TEMPLATE] }),
        queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_CHANNEL] }),
      ]),
    [queryClient],
  );

  // Undefined when service map is off, and the payload requires it, so it falls back as the header does.
  const serviceName = useRequestService() ?? DEFAULT_SERVICE;
  const applicationName = selectedApplication?.applicationName || '';
  const selectedApplicationServiceType = selectedApplication?.serviceType;
  const selectedApplicationKey: AlarmV2Rule.Parameters = React.useMemo(
    () => ({
      applicationName: selectedApplication?.applicationName,
      applicationType: selectedApplication?.serviceType,
    }),
    [selectedApplication?.applicationName, selectedApplication?.serviceType],
  );
  const applicationKeyOf = React.useCallback(
    (rule?: Partial<AlarmV2Rule.RuleData>): AlarmV2Rule.Parameters | undefined =>
      rule?.applicationName
        ? {
            applicationName: rule.applicationName,
            applicationType: rule.applicationType,
          }
        : undefined,
    [],
  );

  const { data: channelsData, isLoading: isChannelsLoading } = useAlarmV2ChannelQuery();
  const { data: templatesData } = useAlarmV2TemplateQuery();
  const { data: applicationDataSources } = useAlarmV2DataSourcesQuery(
    selectedApplicationServiceType,
  );
  // Only the bundles whose every item measures what this application's data sources offer.
  const applicableTemplates = React.useMemo(
    () =>
      applicationDataSources &&
      templatesData?.filter((template) =>
        (template.items ?? []).every((item) =>
          applicationDataSources.some((ds) => ds.value === item.dataSource),
        ),
      ),
    [templatesData, applicationDataSources],
  );
  // Read the rules here rather than through the Rules tab list: both the ?ruleId=
  // deep link and the template start view need them before that tab has mounted.
  const { data: rulesData } = useAlarmV2RuleQuery(selectedApplicationKey);

  // A bundle already applied to this application cannot be applied again -- the
  // server answers 409 -- so the picker shows it disabled rather than letting the
  // user pick it and fail. Read off the rules, the same way the Rules tab groups them.
  const appliedTemplateIds = React.useMemo(
    () => new Set((rulesData ?? []).flatMap((rule) => (rule.templateId ? [rule.templateId] : []))),
    [rulesData],
  );
  // The rule and channel being viewed are resolved from the URL so the full-page
  // views survive reload and back navigation, like the template views. A rule stays
  // the same object across list refetches only by id; the form reads it once on
  // mount and is keyed by that id.
  const currentTargetRule = React.useMemo<Partial<AlarmV2Rule.RuleData> | undefined>(() => {
    if (view === 'rule-new') {
      return {
        serviceName,
        applicationName,
        applicationType: selectedApplication?.serviceType,
        severity: 'WARNING',
        enabled: true,
        checkIntervalSec: DEFAULT_CHECK_INTERVAL_SEC,
        actionIntervalSec: DEFAULT_ACTION_INTERVAL_SEC,
      };
    }
    if (view === 'rule-edit') {
      return rulesData?.find((rule) => rule.id === viewRuleId);
    }
    return undefined;
  }, [view, viewRuleId, rulesData, serviceName, applicationName, selectedApplication?.serviceType]);
  const currentTargetChannel = React.useMemo<
    Partial<AlarmV2Channel.ChannelData> | undefined
  >(() => {
    if (view === 'channel-new') {
      return { methodType: 'EMAIL' };
    }
    if (view === 'channel-edit') {
      return channelsData?.find((channel) => channel.id === viewChannelId);
    }
    return undefined;
  }, [view, viewChannelId, channelsData]);
  const { data: presetsData } = useAlarmV2TemplatePresetsQuery(
    isTemplateNewView,
    selectedApplication?.serviceType,
  );
  const editingTemplate = React.useMemo(
    () => templatesData?.find((template) => template.id === viewTemplateId),
    [templatesData, viewTemplateId],
  );
  const { data: linkedChannelsData, isLoading: isLinkedChannelsLoading } =
    useAlarmV2ChannelsByRuleQuery(
      currentTargetRule?.templateItemId ? undefined : currentTargetRule?.id,
      currentTargetRule?.templateItemId ? undefined : applicationKeyOf(currentTargetRule),
    );
  const linkedChannelIds = React.useMemo(
    () => channelIdsOf(linkedChannelsData),
    [linkedChannelsData],
  );
  const { data: inheritedRuleChannelsData, isLoading: isInheritedRuleChannelsLoading } =
    useAlarmV2ChannelsByTemplateQuery(currentTargetRule?.templateId);
  const inheritedRuleChannelIds = React.useMemo(
    () => channelIdsOf(inheritedRuleChannelsData),
    [inheritedRuleChannelsData],
  );
  const { data: linkedTemplateChannelsData, isLoading: isLinkedTemplateChannelsLoading } =
    useAlarmV2ChannelsByTemplateQuery(viewTemplateId);
  const linkedTemplateChannelIds = React.useMemo(
    () => channelIdsOf(linkedTemplateChannelsData),
    [linkedTemplateChannelsData],
  );
  const { data: ruleDetailStateData } = useAlarmV2RuleStateQuery(
    currentTargetRule?.id,
    applicationKeyOf(currentTargetRule),
  );
  // The rule list response omits the nested template payload for size;
  // fetch it lazily only when editing a template-linked rule, where the form needs it
  // to show the template even if it has since been deleted.
  const { data: ruleDetailData } = useAlarmV2RuleDetailQuery(
    currentTargetRule?.templateItemId ? currentTargetRule?.id : undefined,
    currentTargetRule?.templateItemId ? applicationKeyOf(currentTargetRule) : undefined,
  );
  const ruleFormData = React.useMemo(
    () =>
      currentTargetRule?.id &&
      currentTargetRule?.templateItemId &&
      ruleDetailData?.id === currentTargetRule.id
        ? ruleDetailData
        : currentTargetRule,
    [currentTargetRule, ruleDetailData],
  );
  const { data: historyData } = useAlarmV2RuleHistoryQuery(
    currentHistoryRule?.id,
    applicationKeyOf(currentHistoryRule),
  );
  const { data: historyRuleStateData } = useAlarmV2RuleStateQuery(
    currentHistoryRule?.id,
    applicationKeyOf(currentHistoryRule),
  );
  const { data: userGroupList } = useGetUserGroup({}, { enabled: !!currentTargetChannel });
  const appNameParam = searchParams.get('applicationName');
  React.useEffect(() => {
    if (!appNameParam) return;

    // The type is read off the list rather than assumed: which application types can carry
    // a rule is up to the deployment, and a guess here would query for the wrong one until
    // the effect below corrected it.
    const linked = applications?.find((item) => item.applicationName === appNameParam);
    if (!linked) return;

    setSelectedApplication((current) =>
      current?.applicationName === linked.applicationName &&
      current?.serviceType === linked.serviceType
        ? current
        : linked,
    );
  }, [appNameParam, applications, setSelectedApplication]);

  React.useEffect(() => {
    const ruleIdParam = searchParams.get('ruleId');
    if (ruleIdParam && !view && rulesData) {
      const rule = rulesData.find((r) => String(r.id) === ruleIdParam);
      if (rule) setCurrentHistoryRule(rule);
    }
  }, [searchParams, view, rulesData]);

  const { mutate: mutateRule } = useAlarmV2RuleMutation({
    onSuccess: () => {
      toast.success(t('CONFIGURATION.ALARM_V2.RULE_DELETED'));
      setCurrentDeletingRule(undefined);
      closeView();
      void invalidateAlarmQueries();
    },
    onError: () => {
      toast.error(t('CONFIGURATION.ALARM_V2.RULE_DELETE_FAILED'));
    },
  });

  const { mutate: mutateRuleToggle } = useAlarmV2RuleMutation({
    onSuccess: () => {
      toast.success(t('CONFIGURATION.ALARM_V2.RULE_UPDATED'));
      void invalidateAlarmQueries();
    },
    onError: () => {
      toast.error(t('CONFIGURATION.ALARM_V2.RULE_UPDATE_FAILED'));
    },
  });

  const { mutateAsync: mutateRuleSaveAsync, isPending: isRuleSavePending } =
    useAlarmV2RuleMutation();
  const { mutateAsync: mutateChannelLinkAsync } = useAlarmV2ChannelMutation();
  const { mutateAsync: mutateTemplateSaveAsync, isPending: isTemplateSavePending } =
    useAlarmV2TemplateMutation();

  const { mutate: mutateChannel } = useAlarmV2ChannelMutation({
    onSuccess: () => {
      toast.success(t('CONFIGURATION.ALARM_V2.CHANNEL_DELETED'));
      setCurrentDeletingChannel(undefined);
      closeView();
      void invalidateAlarmQueries();
    },
    onError: () => {
      toast.error(t('CONFIGURATION.ALARM_V2.CHANNEL_DELETE_FAILED'));
    },
  });

  const { mutateAsync: mutateTemplateApplyAsync, isPending: isTemplateApplyPending } =
    useAlarmV2TemplateApplyMutation();

  const handleApplyTemplate = async (template: AlarmV2Template.TemplateData) => {
    if (!template.id || !selectedApplication?.applicationName || !selectedApplication.serviceType) {
      return;
    }
    try {
      await mutateTemplateApplyAsync({
        method: 'APPLY',
        id: template.id,
        applicationName,
        applicationType: selectedApplication.serviceType,
      });
      setIsApplyPickerOpen(false);
      toast.success(t('CONFIGURATION.ALARM_V2.TEMPLATE_APPLIED'));
      await invalidateAlarmQueries();
    } catch {
      toast.error(t('CONFIGURATION.ALARM_V2.TEMPLATE_APPLY_FAILED'));
    }
  };

  const handleUnapplyTemplate = async () => {
    if (
      !unlinkingTemplate ||
      !selectedApplication?.applicationName ||
      !selectedApplication.serviceType
    ) {
      return;
    }
    try {
      await mutateTemplateApplyAsync({
        method: 'UNAPPLY',
        id: unlinkingTemplate.templateId,
        applicationName,
        applicationType: selectedApplication.serviceType,
      });
      setUnlinkingTemplate(undefined);
      toast.success(t('CONFIGURATION.ALARM_V2.TEMPLATE_UNAPPLIED'));
      await invalidateAlarmQueries();
    } catch {
      toast.error(t('CONFIGURATION.ALARM_V2.TEMPLATE_UNAPPLY_FAILED'));
    }
  };

  const { mutate: mutateTemplate } = useAlarmV2TemplateMutation({
    onSuccess: () => {
      toast.success(t('CONFIGURATION.ALARM_V2.TEMPLATE_DELETED'));
      setCurrentDeletingTemplate(undefined);
      void invalidateAlarmQueries();
    },
    onError: () => {
      toast.error(t('CONFIGURATION.ALARM_V2.TEMPLATE_DELETE_FAILED'));
    },
  });

  const openStandaloneRuleForm = () => navigate('?view=rule-new');
  const openRuleView = (rule: AlarmV2Rule.RuleData) => {
    if (hasPermission && rule.id) navigate(`?view=rule-edit&ruleId=${rule.id}`);
  };
  const openChannelView = (channel: AlarmV2Channel.ChannelData) => {
    if (hasPermission && channel.id) navigate(`?view=channel-edit&channelId=${channel.id}`);
  };

  const openTemplateEditForm = (template: AlarmV2Template.TemplateData) => {
    if (!hasPermission || !template.id) {
      return;
    }
    setDraftTemplateItems(undefined);
    navigate(`?view=template-edit&templateId=${template.id}`);
  };

  const openTemplateDeleteDialog = (template: AlarmV2Template.TemplateData) => {
    setCurrentDeletingTemplate(template);
  };

  /** Links and unlinks channels until they match the selection; false when any call failed. */
  const syncChannelLinks = async (
    linkedIds: number[],
    selectedIds: number[],
    mutateLink: (channelId: number, link: boolean) => Promise<unknown>,
    createdEditPath?: string,
  ) => {
    const results = await Promise.allSettled([
      ...selectedIds.filter((id) => !linkedIds.includes(id)).map((id) => mutateLink(id, true)),
      ...linkedIds.filter((id) => !selectedIds.includes(id)).map((id) => mutateLink(id, false)),
    ]);
    if (results.every((r) => r.status === 'fulfilled')) {
      return true;
    }
    if (createdEditPath) {
      navigate(createdEditPath);
    }
    toast.error(t('CONFIGURATION.ALARM_V2.CHANNEL_LINK_FAILED'));
    await invalidateAlarmQueries();
    return false;
  };

  const handleRuleSave = async (values: FormValues, selectedChannelIds: number[]) => {
    try {
      const ruleData = buildRuleSaveData(values, {
        serviceName,
        applicationName,
        applicationType: currentTargetRule?.applicationType ?? selectedApplication?.serviceType,
      });
      let ruleId: number;

      if (currentTargetRule?.id) {
        await mutateRuleSaveAsync({ method: 'PUT', id: currentTargetRule.id, params: ruleData });
        ruleId = currentTargetRule.id;
      } else {
        const result = await mutateRuleSaveAsync({ method: 'POST', params: ruleData });
        if (!result?.id) {
          toast.error(t('CONFIGURATION.ALARM_V2.RULE_UPDATE_FAILED'));
          return;
        }
        ruleId = result.id;
      }

      if (!ruleData.templateItemId) {
        const linked = await syncChannelLinks(
          linkedChannelIds,
          selectedChannelIds,
          (channelId, link) =>
            mutateChannelLinkAsync({
              method: link ? 'LINK' : 'UNLINK',
              ruleId,
              channelId,
              applicationName,
              applicationType: ruleData.applicationType,
            }),
          currentTargetRule?.id ? undefined : `?view=rule-edit&ruleId=${ruleId}`,
        );
        if (!linked) return;
      }

      toast.success(
        currentTargetRule?.id
          ? t('CONFIGURATION.ALARM_V2.RULE_UPDATED')
          : t('COMMON.CREATE_SUCCESS'),
      );
      closeView();
      await invalidateAlarmQueries();
    } catch {
      toast.error(t('CONFIGURATION.ALARM_V2.RULE_UPDATE_FAILED'));
    }
  };

  /**
   * Editing a bundle rewrites the rules it already stamped, in every application it
   * is applied to -- a change the user cannot see from this page. Confirm first, and
   * only when there is something out there to change.
   */
  const handleTemplateSave = (
    templateData: Pick<AlarmV2Template.TemplateData, 'name' | 'description' | 'items'>,
    selectedChannelIds: number[],
  ) => {
    const appliedRuleCount = editingTemplate?.usedRuleCount ?? 0;
    if (editingTemplate?.id && appliedRuleCount > 0) {
      setPendingTemplateSave({ templateData, selectedChannelIds });
      return;
    }
    void runTemplateSave(templateData, selectedChannelIds);
  };

  const runTemplateSave = async (
    templateData: Pick<AlarmV2Template.TemplateData, 'name' | 'description' | 'items'>,
    selectedChannelIds: number[],
  ) => {
    try {
      let templateId: number;

      if (editingTemplate?.id) {
        await mutateTemplateSaveAsync({
          method: 'PUT',
          id: editingTemplate.id,
          applicationName,
          params: templateData,
        });
        templateId = editingTemplate.id;
      } else {
        const result = await mutateTemplateSaveAsync({
          method: 'POST',
          applicationName,
          params: templateData,
        });
        if (!result?.id) {
          toast.error(t('CONFIGURATION.ALARM_V2.TEMPLATE_UPDATE_FAILED'));
          return;
        }
        templateId = result.id;
      }

      const linked = await syncChannelLinks(
        linkedTemplateChannelIds,
        selectedChannelIds,
        (channelId, link) =>
          mutateChannelLinkAsync({
            method: link ? 'LINK_TEMPLATE' : 'UNLINK_TEMPLATE',
            templateId,
            channelId,
            applicationName,
          }),
        editingTemplate?.id ? undefined : `?view=template-edit&templateId=${templateId}`,
      );
      if (!linked) return;

      toast.success(
        editingTemplate?.id
          ? t('CONFIGURATION.ALARM_V2.TEMPLATE_UPDATED')
          : t('COMMON.CREATE_SUCCESS'),
      );
      closeView();
      await invalidateAlarmQueries();
    } catch {
      toast.error(t('CONFIGURATION.ALARM_V2.TEMPLATE_UPDATE_FAILED'));
    }
  };

  const { mutate: mutateChannelSave, isPending: isChannelSavePending } = useAlarmV2ChannelMutation({
    onSuccess: () => {
      toast.success(
        currentTargetChannel?.id
          ? t('CONFIGURATION.ALARM_V2.CHANNEL_UPDATED')
          : t('COMMON.CREATE_SUCCESS'),
      );
      closeView();
      void queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_CHANNEL] });
    },
    onError: () => {
      toast.error(t('CONFIGURATION.ALARM_V2.CHANNEL_UPDATE_FAILED'));
    },
  });

  if (isRuleView) {
    return (
      <AlarmV2FullPage
        onBack={closeView}
        title={
          <AlarmV2RuleSheetTitle
            title={
              currentTargetRule?.id
                ? t('CONFIGURATION.ALARM_V2.RULE_DETAIL')
                : t('CONFIGURATION.ALARM_V2.ADD_RULE')
            }
            rule={currentTargetRule}
            status={ruleDetailStateData?.status}
          />
        }
      >
        {currentTargetRule ? (
          <AlarmV2RuleForm
            key={view === 'rule-new' ? 'rule-new' : `rule-${viewRuleId}`}
            data={ruleFormData}
            channels={currentTargetRule?.templateItemId ? inheritedRuleChannelsData : channelsData}
            isChannelsLoading={
              currentTargetRule?.templateItemId
                ? isInheritedRuleChannelsLoading
                : isChannelsLoading || isLinkedChannelsLoading
            }
            linkedChannelIds={
              currentTargetRule?.templateItemId ? inheritedRuleChannelIds : linkedChannelIds
            }
            mode="rule"
            applicationType={currentTargetRule?.applicationType ?? selectedApplicationServiceType}
            pending={isRuleSavePending}
            onCancel={closeView}
            onSubmit={handleRuleSave}
          />
        ) : rulesData ? (
          <AlarmV2EmptyState>{t('COMMON.NO_DATA')}</AlarmV2EmptyState>
        ) : (
          <DataTableSkeleton hideRowBox />
        )}
      </AlarmV2FullPage>
    );
  }

  if (isChannelView) {
    return (
      <AlarmV2FullPage
        onBack={closeView}
        title={
          <span>
            {currentTargetChannel?.id
              ? t('CONFIGURATION.ALARM_V2.EDIT_CHANNEL')
              : t('CONFIGURATION.ALARM_V2.ADD_CHANNEL')}
          </span>
        }
      >
        {currentTargetChannel ? (
          <AlarmV2ChannelForm
            key={view === 'channel-new' ? 'channel-new' : `channel-${viewChannelId}`}
            data={currentTargetChannel}
            userGroups={userGroupList}
            pending={isChannelSavePending}
            onCancel={closeView}
            onSubmit={(values) => {
              if (currentTargetChannel?.id) {
                mutateChannelSave({
                  method: 'PUT',
                  id: currentTargetChannel.id,
                  applicationName,
                  params: values,
                });
              } else {
                mutateChannelSave({
                  method: 'POST',
                  applicationName,
                  params: values,
                });
              }
            }}
          />
        ) : channelsData ? (
          <AlarmV2EmptyState>{t('COMMON.NO_DATA')}</AlarmV2EmptyState>
        ) : (
          <DataTableSkeleton hideRowBox />
        )}
      </AlarmV2FullPage>
    );
  }

  if (isTemplateNewView || isTemplateEditView) {
    const title = isTemplateNewView
      ? t('CONFIGURATION.ALARM_V2.ADD_TEMPLATE')
      : t('CONFIGURATION.ALARM_V2.TEMPLATE_DETAIL');
    // The start view runs first for a new bundle; picking sources hands its items here.
    const showStartView = isTemplateNewView && draftTemplateItems === undefined;

    // The edit view seeds its state from the template once, at mount, so it must not
    // mount before the list has arrived (a reload lands here with no data yet).
    if (isTemplateEditView && !editingTemplate) {
      return (
        <AlarmV2FullPage onBack={closeView} title={<span>{title}</span>}>
          {templatesData ? (
            <AlarmV2EmptyState>{t('COMMON.NO_DATA')}</AlarmV2EmptyState>
          ) : (
            <DataTableSkeleton hideRowBox />
          )}
        </AlarmV2FullPage>
      );
    }

    return (
      <AlarmV2FullPage onBack={closeView} title={<span>{title}</span>}>
        {showStartView ? (
          <AlarmV2TemplateStartView
            presets={presetsData}
            templates={templatesData}
            standaloneRules={rulesData}
            onCancel={closeView}
            onContinue={(items: AlarmV2Template.TemplateItemData[]) => setDraftTemplateItems(items)}
          />
        ) : (
          <AlarmV2TemplateEditView
            key={isTemplateEditView ? `edit-${viewTemplateId}` : 'new'}
            template={
              isTemplateEditView
                ? editingTemplate
                : {
                    serviceName,
                    name: '',
                    items: draftTemplateItems ?? [],
                  }
            }
            channels={channelsData}
            linkedChannelIds={isTemplateEditView ? linkedTemplateChannelIds : NO_LINKED_CHANNEL_IDS}
            isChannelsLoading={isChannelsLoading || isLinkedTemplateChannelsLoading}
            pending={isTemplateSavePending}
            disabled={!hasPermission}
            onCancel={closeView}
            onSubmit={handleTemplateSave}
          />
        )}

        <AlarmV2ConfirmDialog
          open={!!pendingTemplateSave}
          destructive={false}
          onOpenChange={(open) => {
            if (!open) setPendingTemplateSave(undefined);
          }}
          title={t('CONFIGURATION.ALARM_V2.SAVE_TEMPLATE_TITLE', {
            name: editingTemplate?.name,
          })}
          description={t('CONFIGURATION.ALARM_V2.SAVE_TEMPLATE_DESC', {
            applicationCount: editingTemplate?.usedApplicationCount ?? 0,
            ruleCount: editingTemplate?.usedRuleCount ?? 0,
          })}
          disabled={isTemplateSavePending}
          onConfirm={() => {
            const pending = pendingTemplateSave;
            setPendingTemplateSave(undefined);
            if (pending) {
              void runTemplateSave(pending.templateData, pending.selectedChannelIds);
            }
          }}
        />
      </AlarmV2FullPage>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">{t('CONFIGURATION.ALARM_V2.NAME')}</h3>
        <p className="text-sm text-muted-foreground">{t('CONFIGURATION.ALARM_V2.DESC')}</p>
      </div>
      <div className="flex items-center gap-2">
        {ApplicationList && (
          <ApplicationList
            selectedApplication={selectedApplication}
            onClickApplication={(app) => setSelectedApplication(app)}
          />
        )}
      </div>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="rules">{t('CONFIGURATION.ALARM_V2.RULES')}</TabsTrigger>
          <TabsTrigger value="channels">{t('CONFIGURATION.ALARM_V2.CHANNELS')}</TabsTrigger>
          <TabsTrigger value="templates">{t('CONFIGURATION.ALARM_V2.TEMPLATES')}</TabsTrigger>
        </TabsList>
        <TabsContent value="rules">
          <Separator className="mb-6" />
          <div className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <AlarmV2TableSearch
                value={ruleSearchQuery}
                placeholder={t('CONFIGURATION.ALARM_V2.SEARCH_RULES')}
                onChange={setRuleSearchQuery}
              />
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={
                    !selectedApplication || !selectedApplication?.applicationName || !hasPermission
                  }
                  onClick={openStandaloneRuleForm}
                >
                  <MdOutlineAdd className="mr-1" />
                  {t('CONFIGURATION.ALARM_V2.ADD_RULE')}
                </Button>
                <Button
                  variant="outline"
                  disabled={
                    !selectedApplication ||
                    !selectedApplication?.applicationName ||
                    !hasPermission ||
                    !applicableTemplates?.length
                  }
                  onClick={() => setIsApplyPickerOpen(true)}
                >
                  <MdOutlineAdd className="mr-1" />
                  {t('CONFIGURATION.ALARM_V2.ADD_FROM_TEMPLATE')}
                </Button>
              </div>
            </div>
            <AlarmV2RuleList
              applicationName={selectedApplication?.applicationName}
              applicationType={selectedApplication?.serviceType}
              rowFilterInfo={{ query: ruleSearchQuery }}
              disabled={!hasPermission}
              onClickRowItem={openRuleView}
              onClickEdit={openRuleView}
              onClickDelete={(data) => setCurrentDeletingRule(data)}
              onClickHistory={(data) => setCurrentHistoryRule(data)}
              onUnlinkTemplate={(templateId, templateName, ruleCount) =>
                setUnlinkingTemplate({ templateId, templateName, ruleCount })
              }
              onToggleEnabled={(data, enabled) => {
                if (data.id) {
                  mutateRuleToggle({
                    method: 'PATCH',
                    id: data.id,
                    applicationName: data.applicationName,
                    applicationType: data.applicationType,
                    enabled,
                  });
                }
              }}
            />
          </div>
        </TabsContent>
        <TabsContent value="channels">
          <Separator className="mb-6" />
          <div className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <AlarmV2TableSearch
                value={channelSearchQuery}
                placeholder={t('CONFIGURATION.ALARM_V2.SEARCH_CHANNELS')}
                onChange={setChannelSearchQuery}
              />
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={!hasPermission || !selectedApplication?.applicationName}
                  onClick={() => navigate('?view=channel-new')}
                >
                  <MdOutlineAdd className="mr-1" />
                  {t('CONFIGURATION.ALARM_V2.ADD_CHANNEL')}
                </Button>
              </div>
            </div>
            <AlarmV2ChannelList
              rowFilterInfo={{ query: channelSearchQuery }}
              disabled={!hasPermission}
              onClickRowItem={openChannelView}
              onClickEdit={openChannelView}
              onClickDelete={(data) => setCurrentDeletingChannel(data)}
            />
          </div>
        </TabsContent>
        <TabsContent value="templates">
          <Separator className="mb-6" />
          <div className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <AlarmV2TableSearch
                value={templateSearchQuery}
                placeholder={t('CONFIGURATION.ALARM_V2.SEARCH_TEMPLATES')}
                onChange={setTemplateSearchQuery}
              />
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={!hasPermission || !selectedApplication?.applicationName}
                  onClick={() => {
                    setDraftTemplateItems(undefined);
                    navigate('?view=template-new');
                  }}
                >
                  <MdOutlineAdd className="mr-1" />
                  {t('CONFIGURATION.ALARM_V2.ADD_TEMPLATE')}
                </Button>
              </div>
            </div>
            <AlarmV2TemplateList
              rowFilterInfo={{ query: templateSearchQuery }}
              disabled={!hasPermission}
              onClickRowItem={openTemplateEditForm}
              onClickEdit={openTemplateEditForm}
              onClickDelete={openTemplateDeleteDialog}
            />
          </div>
        </TabsContent>
      </Tabs>

      <AlarmV2ConfirmDialog
        open={!!currentDeletingRule}
        onOpenChange={(open) => {
          if (!open) setCurrentDeletingRule(undefined);
        }}
        title={t('CONFIGURATION.ALARM_V2.DELETE_RULE_TITLE', { name: currentDeletingRule?.name })}
        description={t('CONFIGURATION.ALARM_V2.DELETE_RULE_DESC')}
        disabled={!hasPermission}
        onConfirm={() => {
          if (currentDeletingRule?.id) {
            mutateRule({
              method: 'DELETE',
              id: currentDeletingRule.id,
              applicationName: currentDeletingRule.applicationName,
              applicationType: currentDeletingRule.applicationType,
            });
          }
        }}
      />

      <AlarmV2ConfirmDialog
        open={!!currentDeletingChannel}
        onOpenChange={(open) => {
          if (!open) setCurrentDeletingChannel(undefined);
        }}
        title={t('CONFIGURATION.ALARM_V2.DELETE_CHANNEL_TITLE', {
          name: currentDeletingChannel?.channelName,
        })}
        description={t('CONFIGURATION.ALARM_V2.DELETE_CHANNEL_DESC', {
          templateCount: currentDeletingChannel?.templateCount ?? 0,
          affectedRuleCount: currentDeletingChannel?.affectedRuleCount ?? 0,
          enabledAffectedRuleCount: currentDeletingChannel?.enabledAffectedRuleCount ?? 0,
        })}
        disabled={!hasPermission}
        onConfirm={() => {
          if (currentDeletingChannel?.id) {
            mutateChannel({
              method: 'DELETE',
              id: currentDeletingChannel.id,
              applicationName,
            });
          }
        }}
      />

      <AlarmV2ConfirmDialog
        open={!!currentDeletingTemplate}
        onOpenChange={(open) => {
          if (!open) setCurrentDeletingTemplate(undefined);
        }}
        title={t('CONFIGURATION.ALARM_V2.DELETE_TEMPLATE_TITLE', {
          name: currentDeletingTemplate?.name,
        })}
        description={t('CONFIGURATION.ALARM_V2.DELETE_TEMPLATE_DESC', {
          count: currentDeletingTemplate?.usedRuleCount ?? 0,
        })}
        disabled={!hasPermission}
        onConfirm={() => {
          if (currentDeletingTemplate?.id) {
            mutateTemplate({
              method: 'DELETE',
              id: currentDeletingTemplate.id,
              applicationName,
            });
          }
        }}
      />

      <AlarmV2Sheet
        open={isApplyPickerOpen}
        onOpenChange={setIsApplyPickerOpen}
        title={t('CONFIGURATION.ALARM_V2.APPLY_TEMPLATE')}
      >
        {applicableTemplates?.length ? (
          <div className="space-y-2">
            {applicableTemplates.map((template) => {
              const isApplied = template.id !== undefined && appliedTemplateIds.has(template.id);
              return (
                <button
                  key={template.id}
                  type="button"
                  disabled={isTemplateApplyPending || isApplied}
                  className="w-full rounded-md border bg-background p-3 text-left transition-colors hover:bg-accent disabled:pointer-events-none disabled:opacity-60"
                  onClick={() => handleApplyTemplate(template)}
                >
                  <div className="flex items-center gap-2">
                    <span className="truncate text-sm font-medium">{template.name}</span>
                    {isApplied && (
                      <Badge variant="outline" className="shrink-0 font-normal">
                        {t('CONFIGURATION.ALARM_V2.ALREADY_APPLIED')}
                      </Badge>
                    )}
                  </div>
                  {template.description && (
                    <div className="line-clamp-2 text-xs text-muted-foreground">
                      {template.description}
                    </div>
                  )}
                  <div className="mt-2 flex flex-wrap gap-2">
                    {(template.items ?? []).map((item) => (
                      <Badge key={item.id ?? item.name} variant="outline" className="font-normal">
                        {item.name}
                      </Badge>
                    ))}
                  </div>
                </button>
              );
            })}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">
            {t('CONFIGURATION.ALARM_V2.NO_TEMPLATES_AVAILABLE')}
          </p>
        )}
      </AlarmV2Sheet>

      <AlarmV2ConfirmDialog
        open={!!unlinkingTemplate}
        onOpenChange={(open) => {
          if (!open) setUnlinkingTemplate(undefined);
        }}
        title={t('CONFIGURATION.ALARM_V2.UNLINK_TEMPLATE_TITLE', {
          name: unlinkingTemplate?.templateName,
        })}
        description={t('CONFIGURATION.ALARM_V2.UNLINK_TEMPLATE_DESC', {
          count: unlinkingTemplate?.ruleCount ?? 0,
        })}
        disabled={!hasPermission}
        onConfirm={handleUnapplyTemplate}
      />

      <AlarmV2HistorySheet
        rule={currentHistoryRule}
        history={historyData}
        status={historyRuleStateData?.status}
        onOpenChange={(open) => {
          if (open) return;
          setCurrentHistoryRule(undefined);
          // The deep-link parameter has to go with the sheet. Left behind, the effect that
          // opens the sheet from it fires again on the next rule list refetch and reopens
          // what the user just closed -- over whichever rule they picked in the meantime.
          if (searchParams.has('ruleId')) {
            searchParams.delete('ruleId');
            setSearchParams(searchParams, { replace: true });
          }
        }}
      />
    </div>
  );
};
