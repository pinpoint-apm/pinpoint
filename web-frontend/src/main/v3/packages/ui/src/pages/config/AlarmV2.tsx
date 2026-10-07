import React from 'react';
import { AlarmV2RulesTab } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RulesTab';
import { AlarmV2ChannelsTab } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ChannelsTab';
import { AlarmV2TemplatesTab } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplatesTab';
import { AlarmV2RuleFullPage } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleFullPage';
import { AlarmV2ChannelFullPage } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ChannelFullPage';
import { AlarmV2TemplateFullPage } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateFullPage';
import { useTranslation } from 'react-i18next';
import {
  AlarmV2Rule,
  AlarmV2Channel,
  AlarmV2Template,
  ApplicationType,
} from '@pinpoint-fe/ui/src/constants/types';
import { Separator } from '@pinpoint-fe/ui/src/components/ui/separator';
import { APP_SETTING_KEYS, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2HistorySheet } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2HistorySheet';
import { AlarmV2Sheet } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2Sheet';
import { AlarmV2ConfirmDialog } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ConfirmDialog';
import {
  DEFAULT_ACTION_INTERVAL_SEC,
  DEFAULT_CHECK_INTERVAL_SEC,
} from '@pinpoint-fe/ui/src/components/AlarmV2/formatInterval';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@pinpoint-fe/ui/src/components/ui/tabs';
import { Badge } from '@pinpoint-fe/ui/src/components/ui/badge';
import { useReactToastifyToast } from '@pinpoint-fe/ui/src/components/Toast';
import { useIsForbiddenPath } from '@pinpoint-fe/ui/src/hooks';
import { useLocalStorage } from 'usehooks-ts';
import {
  useAlarmV2RuleMutation,
  useAlarmV2ChannelMutation,
  useAlarmV2ChannelQuery,
  useAlarmV2DataSourcesQuery,
  useAlarmV2RuleHistoryQuery,
  useAlarmV2RuleStateQuery,
  useAlarmV2RuleQuery,
  useAlarmV2TemplateQuery,
  useAlarmV2TemplateApplyMutation,
  useAlarmV2TemplateMutation,
} from '@pinpoint-fe/ui/src/hooks/api';
import { useRequestService } from '@pinpoint-fe/ui/src/hooks/utility/useRequestService';
import { DEFAULT_SERVICE, forbiddenPathAtom } from '@pinpoint-fe/ui/src/atoms';
import { useSetAtom } from 'jotai';
import { Forbidden403 } from '../Forbidden403';
import { useNavigate, useSearchParams } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import {
  applicationKeyOf,
  useInvalidateAlarmQueries,
} from '@pinpoint-fe/ui/src/components/AlarmV2/useAlarmV2Saves';
import { useAlarmV2DeepLink } from '@pinpoint-fe/ui/src/components/AlarmV2/useAlarmV2DeepLink';

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

const AlarmV2PageHeader = () => {
  const { t } = useTranslation();
  return (
    <div>
      <h3 className="text-lg font-semibold">{t('CONFIGURATION.ALARM_V2.NAME')}</h3>
      <p className="text-sm text-muted-foreground">{t('CONFIGURATION.ALARM_V2.DESC')}</p>
    </div>
  );
};

/**
 * The notice replaces the body instead of hiding it, so that the body's queries stop being
 * observed while it is up. {@link enterForbiddenNotice} removes a 403 that arrives then from
 * the cache, and an observer that is still there asks again at once, which never stops.
 */
export const AlarmV2Page = (props: AlarmV2PageProps) => {
  const isForbidden = useIsForbiddenPath();

  if (isForbidden) {
    return (
      <div className="space-y-6">
        <AlarmV2PageHeader />
        <Forbidden403 />
      </div>
    );
  }
  return <AlarmV2PageBody {...props} />;
};

const AlarmV2PageBody = ({
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
  const [currentDeletingTemplate, setCurrentDeletingTemplate] =
    React.useState<AlarmV2Template.TemplateData>();
  const [currentHistoryRule, setCurrentHistoryRule] = React.useState<AlarmV2Rule.RuleData>();
  const [isApplyPickerOpen, setIsApplyPickerOpen] = React.useState(false);
  const [unlinkingTemplate, setUnlinkingTemplate] = React.useState<{
    templateId: number;
    templateName: string;
    ruleCount: number;
  }>();

  const closeView = React.useCallback(() => navigate(''), [navigate]);

  // Rules, bundles and channels all show counts derived from the other two, so every
  // write refreshes the set rather than the one list it touched.
  const invalidateAlarmQueries = useInvalidateAlarmQueries();

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

  const {
    view,
    viewTemplateId,
    viewRuleId,
    viewChannelId,
    isTemplateNewView,
    isTemplateEditView,
    isRuleView,
    isChannelView,
  } = useAlarmV2DeepLink({
    searchParams,
    applications,
    rules: rulesData,
    selectApplication: setSelectedApplication,
    openHistory: setCurrentHistoryRule,
  });

  const setForbiddenPath = useSetAtom(forbiddenPathAtom);
  // A 403 is kept until the path changes, and picking an application does not change it.
  // The rules of the application just left are cancelled: their late 403 would still count as
  // this path's and cover the new one.
  React.useEffect(() => {
    void queryClient.cancelQueries({
      queryKey: [END_POINTS.ALARM_V2_RULE],
      predicate: (query) => query.getObserversCount() === 0,
    });
    setForbiddenPath(undefined);
  }, [selectedApplicationKey, setForbiddenPath, queryClient]);

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
  const { data: historyData } = useAlarmV2RuleHistoryQuery(
    currentHistoryRule?.id,
    applicationKeyOf(currentHistoryRule),
  );
  const { data: historyRuleStateData } = useAlarmV2RuleStateQuery(
    currentHistoryRule?.id,
    applicationKeyOf(currentHistoryRule),
  );
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
    if (hasPermission && applicationName && channel.id) {
      navigate(`?view=channel-edit&channelId=${channel.id}`);
    }
  };

  const openTemplateEditForm = (template: AlarmV2Template.TemplateData) => {
    if (!hasPermission || !template.id) {
      return;
    }
    navigate(`?view=template-edit&templateId=${template.id}`);
  };

  const openTemplateDeleteDialog = (template: AlarmV2Template.TemplateData) => {
    setCurrentDeletingTemplate(template);
  };

  /**
   * The picker stays with the rules, and not above the tabs. Only a rule belongs to an
   * application. A channel and a template belong to the service, and all the applications
   * in the service use them. Above the tabs, the picker looked like a control for all
   * three tabs.
   */
  const applicationPicker = ApplicationList && (
    <div className="flex items-center gap-2">
      <ApplicationList
        selectedApplication={selectedApplication}
        onClickApplication={(app) => setSelectedApplication(app)}
      />
    </div>
  );

  const pageHeader = <AlarmV2PageHeader />;

  if (isRuleView) {
    return (
      <AlarmV2RuleFullPage
        onClose={closeView}
        serviceName={serviceName}
        applicationName={applicationName}
        currentTargetRule={currentTargetRule}
        rulesData={rulesData}
        selectedApplication={selectedApplication}
        channelsData={channelsData}
        isChannelsLoading={isChannelsLoading}
      />
    );
  }

  if (isChannelView) {
    return (
      <AlarmV2ChannelFullPage
        onClose={closeView}
        currentTargetChannel={currentTargetChannel}
        channelsData={channelsData}
        applicationName={applicationName}
      />
    );
  }

  if (isTemplateNewView || isTemplateEditView) {
    return (
      <AlarmV2TemplateFullPage
        key={isTemplateEditView ? `edit-${viewTemplateId}` : 'new'}
        onClose={closeView}
        serviceName={serviceName}
        applicationName={applicationName}
        applicationType={selectedApplication?.serviceType}
        isTemplateNewView={isTemplateNewView}
        isTemplateEditView={isTemplateEditView}
        viewTemplateId={viewTemplateId}
        templatesData={templatesData}
        rulesData={rulesData}
        channelsData={channelsData}
        isChannelsLoading={isChannelsLoading}
        hasPermission={hasPermission}
      />
    );
  }

  return (
    <div className="space-y-6">
      {pageHeader}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="rules">{t('CONFIGURATION.ALARM_V2.RULES')}</TabsTrigger>
          <TabsTrigger value="channels">{t('CONFIGURATION.ALARM_V2.CHANNELS')}</TabsTrigger>
          <TabsTrigger value="templates">{t('CONFIGURATION.ALARM_V2.TEMPLATES')}</TabsTrigger>
        </TabsList>
        <TabsContent value="rules">
          <Separator className="mb-6" />
          <AlarmV2RulesTab
            hasPermission={hasPermission}
            selectedApplication={selectedApplication}
            applicationPicker={applicationPicker}
            hasApplicableTemplate={!!applicableTemplates?.length}
            onAddRule={openStandaloneRuleForm}
            onAddFromTemplate={() => setIsApplyPickerOpen(true)}
            onOpenRule={openRuleView}
            onDeleteRule={setCurrentDeletingRule}
            onOpenHistory={setCurrentHistoryRule}
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
        </TabsContent>
        <TabsContent value="channels">
          <Separator className="mb-6" />
          <AlarmV2ChannelsTab
            hasPermission={hasPermission}
            onAddChannel={() => navigate('?view=channel-new')}
            onOpenChannel={openChannelView}
            onDeleteChannel={setCurrentDeletingChannel}
          />
        </TabsContent>
        <TabsContent value="templates">
          <Separator className="mb-6" />
          <AlarmV2TemplatesTab
            hasPermission={hasPermission}
            onAddTemplate={() => navigate('?view=template-new')}
            onOpenTemplate={openTemplateEditForm}
            onDeleteTemplate={openTemplateDeleteDialog}
          />
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
