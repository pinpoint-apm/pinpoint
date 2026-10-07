import { useTranslation } from 'react-i18next';
import { AlarmV2TemplateEditView } from './AlarmV2TemplateEditView';
import { AlarmV2TemplateStartView } from './AlarmV2TemplateStartView';
import { AlarmV2ConfirmDialog } from './AlarmV2ConfirmDialog';
import React from 'react';
import {
  useAlarmV2ChannelsByTemplateQuery,
  useAlarmV2TemplatePresetsQuery,
} from '@pinpoint-fe/ui/src/hooks/api';
import { channelIdsOf, useAlarmV2TemplateSave } from './useAlarmV2Saves';
import { AlarmV2FullPage, AlarmV2ViewFallback } from './AlarmV2FullPage';
import { AlarmV2Rule, AlarmV2Channel, AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';

// A new bundle has nothing linked yet. Kept at module scope because the editor
// resets its channel selection whenever this prop's identity changes, and an
// inline [] would be a new array on every render.
const NO_LINKED_CHANNEL_IDS: number[] = [];
/**
 * The bundle editor, filling the page. A new bundle starts at the picker, and an edit
 * waits for the list: the editor seeds its state once, at mount.
 */
export interface AlarmV2TemplateFullPageProps {
  onClose: () => void;
  serviceName: string;
  applicationName: string;
  /** The type of the application in the picker; the presets are offered by its category. */
  applicationType?: string;
  isTemplateNewView: boolean;
  isTemplateEditView: boolean;
  viewTemplateId?: number;
  templatesData?: AlarmV2Template.TemplateData[];
  rulesData?: AlarmV2Rule.RuleData[];
  channelsData?: AlarmV2Channel.ChannelData[];
  isChannelsLoading: boolean;
  hasPermission: boolean;
}

export const AlarmV2TemplateFullPage = ({
  onClose,
  serviceName,
  applicationName,
  applicationType,
  isTemplateNewView,
  isTemplateEditView,
  viewTemplateId,
  templatesData,
  rulesData,
  channelsData,
  isChannelsLoading,
  hasPermission,
}: AlarmV2TemplateFullPageProps) => {
  const { t } = useTranslation();
  const [draftTemplateItems, setDraftTemplateItems] =
    React.useState<AlarmV2Template.TemplateItemData[]>();
  const editingTemplate = React.useMemo(
    () => templatesData?.find((template) => template.id === viewTemplateId),
    [templatesData, viewTemplateId],
  );
  const { data: presetsData } = useAlarmV2TemplatePresetsQuery(isTemplateNewView, applicationType);
  const { data: linkedTemplateChannelsData, isLoading: isLinkedTemplateChannelsLoading } =
    useAlarmV2ChannelsByTemplateQuery(viewTemplateId);
  const linkedTemplateChannelIds = React.useMemo(
    () => channelIdsOf(linkedTemplateChannelsData),
    [linkedTemplateChannelsData],
  );
  const {
    saveTemplate,
    runTemplateSave,
    isPending: isTemplateSavePending,
    pendingTemplateSave,
    dismissPendingTemplateSave,
  } = useAlarmV2TemplateSave({
    applicationName,
    editingTemplate,
    templateChannelIds: linkedTemplateChannelIds,
    closeView: onClose,
  });

  const title = isTemplateNewView
    ? t('CONFIGURATION.ALARM_V2.ADD_TEMPLATE')
    : t('CONFIGURATION.ALARM_V2.TEMPLATE_DETAIL');
  // The start view runs first for a new bundle; picking sources hands its items here.
  const showStartView = isTemplateNewView && draftTemplateItems === undefined;

  // The edit view seeds its state from the template once, at mount, so it must not
  // mount before the list has arrived (a reload lands here with no data yet).
  if (isTemplateEditView && !editingTemplate) {
    return (
      <AlarmV2FullPage onBack={onClose} title={<span>{title}</span>}>
        <AlarmV2ViewFallback loaded={!!templatesData} />
      </AlarmV2FullPage>
    );
  }

  return (
    <AlarmV2FullPage onBack={onClose} title={<span>{title}</span>}>
      {showStartView ? (
        <AlarmV2TemplateStartView
          presets={presetsData}
          templates={templatesData}
          standaloneRules={rulesData}
          onCancel={onClose}
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
          onCancel={onClose}
          onSubmit={saveTemplate}
        />
      )}

      <AlarmV2ConfirmDialog
        open={!!pendingTemplateSave}
        destructive={false}
        onOpenChange={(open) => {
          if (!open) dismissPendingTemplateSave();
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
          dismissPendingTemplateSave();
          if (pending) {
            void runTemplateSave(pending.templateData, pending.selectedChannelIds);
          }
        }}
      />
    </AlarmV2FullPage>
  );
};
