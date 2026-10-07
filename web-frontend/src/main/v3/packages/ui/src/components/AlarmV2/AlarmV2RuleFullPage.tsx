import React from 'react';
import { useTranslation } from 'react-i18next';
import { Badge } from '@pinpoint-fe/ui/src/components/ui/badge';
import { ServerIcon } from '@pinpoint-fe/ui/src/components/Application/ServerIcon';
import { formatOverrideKey } from './formatOverrideKey';
import { AlarmV2StateBadge } from './AlarmV2StateBadge';
import { AlarmV2Rule, ApplicationType, AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { AlarmV2FullPage, AlarmV2ViewFallback } from './AlarmV2FullPage';
import { AlarmV2RuleForm } from './AlarmV2RuleForm';
import {
  useAlarmV2ChannelMutation,
  useAlarmV2ChannelsByRuleQuery,
  useAlarmV2ChannelsByTemplateQuery,
  useAlarmV2RuleDetailQuery,
  useAlarmV2RuleMutation,
  useAlarmV2RuleStateQuery,
} from '@pinpoint-fe/ui/src/hooks/api';
import { applicationKeyOf, channelIdsOf, useAlarmV2RuleSave } from './useAlarmV2Saves';

const AlarmV2RuleSheetTitle = ({
  title,
  status,
}: {
  title: React.ReactNode;
  status?: AlarmV2Rule.StateResponse['status'];
}) => (
  <span className="flex min-w-0 flex-1 items-center gap-2">
    {title}
    {status && <AlarmV2StateBadge status={status} />}
  </span>
);
/**
 * This block shows the target of the rule. It comes below the breadcrumb, at the left
 * edge of the page. The title indents its children, and then the reader sees the block
 * as a part of the breadcrumb path.
 */
const AlarmV2RuleSheetMeta = ({
  rule,
  application,
}: {
  /** The application that holds the rule. A new rule has no other sign of it. */
  application?: ApplicationType;
  rule?: Partial<AlarmV2Rule.RuleData>;
}) => {
  const { t } = useTranslation();
  const overrideKeys = rule?.overrideKeys ?? [];

  if (!application?.applicationName && !rule?.templateItemId) {
    return null;
  }

  return (
    <div className="space-y-2">
      {application?.applicationName && (
        <div className="flex min-w-0 items-center gap-2">
          {/* The icon shows the application type. Two applications with different types
              can have the same name. The type is thus a part of the identity of the rule. */}
          <ServerIcon application={application} className="w-6" alt={application.serviceType} />
          <span className="truncate text-xl font-medium">{application.applicationName}</span>
        </div>
      )}
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

/** The rule editor, filling the page. A rule linked to a bundle inherits its channels. */
export interface AlarmV2RuleFullPageProps {
  onClose: () => void;
  serviceName: string;
  applicationName: string;
  currentTargetRule?: Partial<AlarmV2Rule.RuleData>;
  rulesData?: AlarmV2Rule.RuleData[];
  selectedApplication?: ApplicationType;
  channelsData?: AlarmV2Channel.ChannelData[];
  isChannelsLoading: boolean;
}

export const AlarmV2RuleFullPage = ({
  onClose,
  serviceName,
  applicationName,
  currentTargetRule,
  rulesData,
  selectedApplication,
  channelsData,
  isChannelsLoading,
}: AlarmV2RuleFullPageProps) => {
  const { t } = useTranslation();
  // A rule a bundle stamped takes its channels from that bundle, not from its own links.
  const isInherited = !!currentTargetRule?.templateItemId;
  const { data: linkedChannelsData, isLoading: isLinkedChannelsLoading } =
    useAlarmV2ChannelsByRuleQuery(
      isInherited ? undefined : currentTargetRule?.id,
      isInherited ? undefined : applicationKeyOf(currentTargetRule),
    );
  const linkedChannelIds = React.useMemo(
    () => channelIdsOf(linkedChannelsData),
    [linkedChannelsData],
  );
  const { data: inheritedChannelsData, isLoading: isInheritedChannelsLoading } =
    useAlarmV2ChannelsByTemplateQuery(currentTargetRule?.templateId);
  const inheritedChannelIds = React.useMemo(
    () => channelIdsOf(inheritedChannelsData),
    [inheritedChannelsData],
  );
  const { data: ruleStateData } = useAlarmV2RuleStateQuery(
    currentTargetRule?.id,
    applicationKeyOf(currentTargetRule),
  );
  // The rule list response omits the nested template payload for size;
  // fetch it lazily only when editing a template-linked rule, where the form needs it
  // to show the template even if it has since been deleted.
  const { data: ruleDetailData } = useAlarmV2RuleDetailQuery(
    isInherited ? currentTargetRule?.id : undefined,
    isInherited ? applicationKeyOf(currentTargetRule) : undefined,
  );
  const { mutateAsync: saveRuleAsync, isPending: isSavePending } = useAlarmV2RuleMutation();
  const { mutateAsync: linkChannelAsync } = useAlarmV2ChannelMutation();
  const { saveRule } = useAlarmV2RuleSave({
    serviceName,
    applicationName,
    fallbackApplicationType: selectedApplication?.serviceType,
    currentRule: currentTargetRule,
    ruleChannelIds: linkedChannelIds,
    saveRuleAsync,
    linkChannelAsync,
    closeView: onClose,
  });
  const formData =
    currentTargetRule?.id && isInherited && ruleDetailData?.id === currentTargetRule.id
      ? ruleDetailData
      : currentTargetRule;
  // The channels a rule offers come from the bundle that stamped it, or from its own
  // links. The three go together, so the view picks the set once.
  const channelProps = isInherited
    ? {
        channels: inheritedChannelsData,
        linkedChannelIds: inheritedChannelIds,
        isChannelsLoading: isInheritedChannelsLoading,
      }
    : {
        channels: channelsData,
        linkedChannelIds,
        isChannelsLoading: isChannelsLoading || isLinkedChannelsLoading,
      };

  return (
    <AlarmV2FullPage
      onBack={onClose}
      title={
        <AlarmV2RuleSheetTitle
          title={
            currentTargetRule?.id
              ? t('CONFIGURATION.ALARM_V2.RULE_DETAIL')
              : t('CONFIGURATION.ALARM_V2.ADD_RULE')
          }
          status={ruleStateData?.status}
        />
      }
      meta={<AlarmV2RuleSheetMeta rule={currentTargetRule} application={selectedApplication} />}
    >
      {currentTargetRule ? (
        <AlarmV2RuleForm
          key={currentTargetRule.id ? `rule-${currentTargetRule.id}` : 'rule-new'}
          data={formData}
          {...channelProps}
          mode="rule"
          applicationType={currentTargetRule.applicationType ?? selectedApplication?.serviceType}
          pending={isSavePending}
          onCancel={onClose}
          onSubmit={saveRule}
        />
      ) : (
        <AlarmV2ViewFallback loaded={!!rulesData} />
      )}
    </AlarmV2FullPage>
  );
};
