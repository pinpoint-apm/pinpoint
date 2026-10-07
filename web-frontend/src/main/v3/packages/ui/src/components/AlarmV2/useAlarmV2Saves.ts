import React from 'react';
import { useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { AlarmV2Channel, AlarmV2Rule, AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { buildRuleSaveData } from '@pinpoint-fe/ui/src/components/AlarmV2/buildRuleSaveData';
import type { FormValues } from '@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleForm';
import { useReactToastifyToast } from '@pinpoint-fe/ui/src/components/Toast';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import {
  useAlarmV2ChannelMutation,
  useAlarmV2RuleMutation,
  useAlarmV2TemplateMutation,
} from '@pinpoint-fe/ui/src/hooks/api';

/** How a rule names the application that holds it, for the queries keyed by it. */
export const applicationKeyOf = (
  rule?: Partial<AlarmV2Rule.RuleData>,
): AlarmV2Rule.Parameters | undefined =>
  rule?.applicationName
    ? { applicationName: rule.applicationName, applicationType: rule.applicationType }
    : undefined;

/** The ids of the channels a rule or a bundle is linked to. */
export const channelIdsOf = (channels?: AlarmV2Channel.ChannelData[]) =>
  (channels || []).map((c) => c.id).filter((id): id is number => id !== undefined);

/** A bundle save the viewer has yet to confirm. */
export type PendingTemplateSave = {
  templateData: Pick<AlarmV2Template.TemplateData, 'name' | 'description' | 'items'>;
  selectedChannelIds: number[];
};

/** The three lists a save can change. */
export const useInvalidateAlarmQueries = () => {
  const queryClient = useQueryClient();

  return React.useCallback(
    () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_RULE] }),
        queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_TEMPLATE] }),
        queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_CHANNEL] }),
      ]),
    [queryClient],
  );
};

/** Links and unlinks channels until they match the selection; false when any call failed. */
const useSyncChannelLinks = () => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const navigate = useNavigate();
  const invalidateAlarmQueries = useInvalidateAlarmQueries();

  return async (
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
};

type RuleSave = ReturnType<typeof useAlarmV2RuleMutation>['mutateAsync'];

/**
 * Saving a rule: write it, bring its channel links to match the selection, then report
 * and clear the cache.
 */
export const useAlarmV2RuleSave = ({
  serviceName,
  applicationName,
  fallbackApplicationType,
  currentRule,
  ruleChannelIds,
  saveRuleAsync,
  linkChannelAsync,
  closeView,
}: {
  serviceName: string;
  applicationName: string;
  /** The type of the application in the picker, for a rule that does not carry one yet. */
  fallbackApplicationType?: string;
  currentRule?: Partial<AlarmV2Rule.RuleData>;
  ruleChannelIds: number[];
  saveRuleAsync: RuleSave;
  linkChannelAsync: ReturnType<typeof useAlarmV2ChannelMutation>['mutateAsync'];
  closeView: () => void;
}) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const syncChannelLinks = useSyncChannelLinks();
  const invalidateAlarmQueries = useInvalidateAlarmQueries();

  const saveRule = async (values: FormValues, selectedChannelIds: number[]) => {
    try {
      const ruleData = buildRuleSaveData(values, {
        serviceName,
        applicationName,
        applicationType: currentRule?.applicationType ?? fallbackApplicationType,
      });
      let ruleId: number;

      if (currentRule?.id) {
        await saveRuleAsync({ method: 'PUT', id: currentRule.id, params: ruleData });
        ruleId = currentRule.id;
      } else {
        const result = await saveRuleAsync({ method: 'POST', params: ruleData });
        if (!result?.id) {
          toast.error(t('CONFIGURATION.ALARM_V2.RULE_UPDATE_FAILED'));
          return;
        }
        ruleId = result.id;
      }

      if (!ruleData.templateItemId) {
        const linked = await syncChannelLinks(
          ruleChannelIds,
          selectedChannelIds,
          (channelId, link) =>
            linkChannelAsync({
              method: link ? 'LINK' : 'UNLINK',
              ruleId,
              channelId,
              applicationName,
              applicationType: ruleData.applicationType,
            }),
          currentRule?.id ? undefined : `?view=rule-edit&ruleId=${ruleId}`,
        );
        if (!linked) return;
      }

      toast.success(
        currentRule?.id ? t('CONFIGURATION.ALARM_V2.RULE_UPDATED') : t('COMMON.CREATE_SUCCESS'),
      );
      closeView();
      await invalidateAlarmQueries();
    } catch {
      toast.error(t('CONFIGURATION.ALARM_V2.RULE_UPDATE_FAILED'));
    }
  };

  return { saveRule };
};

/** Saving a bundle, and what waits while the viewer answers the question below. */
export const useAlarmV2TemplateSave = ({
  applicationName,
  editingTemplate,
  templateChannelIds,
  closeView,
}: {
  applicationName: string;
  editingTemplate?: AlarmV2Template.TemplateData;
  templateChannelIds: number[];
  closeView: () => void;
}) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const syncChannelLinks = useSyncChannelLinks();
  const invalidateAlarmQueries = useInvalidateAlarmQueries();
  const { mutateAsync: saveTemplateAsync, isPending } = useAlarmV2TemplateMutation();
  const { mutateAsync: linkChannelAsync } = useAlarmV2ChannelMutation();
  const [pendingTemplateSave, setPendingTemplateSave] = React.useState<PendingTemplateSave>();

  const runTemplateSave = async (
    templateData: PendingTemplateSave['templateData'],
    selectedChannelIds: number[],
  ) => {
    try {
      let templateId: number;

      if (editingTemplate?.id) {
        await saveTemplateAsync({
          method: 'PUT',
          id: editingTemplate.id,
          applicationName,
          params: templateData,
        });
        templateId = editingTemplate.id;
      } else {
        const result = await saveTemplateAsync({
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
        templateChannelIds,
        selectedChannelIds,
        (channelId, link) =>
          linkChannelAsync({
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

  /**
   * Editing a bundle rewrites the rules it already stamped, in every application it
   * is applied to -- a change the viewer cannot see from this page. Confirm first, and
   * only when there is something out there to change.
   */
  const saveTemplate = (
    templateData: PendingTemplateSave['templateData'],
    selectedChannelIds: number[],
  ) => {
    const appliedRuleCount = editingTemplate?.usedRuleCount ?? 0;
    if (editingTemplate?.id && appliedRuleCount > 0) {
      setPendingTemplateSave({ templateData, selectedChannelIds });
      return;
    }
    void runTemplateSave(templateData, selectedChannelIds);
  };

  return {
    saveTemplate,
    runTemplateSave,
    isPending,
    pendingTemplateSave,
    dismissPendingTemplateSave: () => setPendingTemplateSave(undefined),
  };
};
