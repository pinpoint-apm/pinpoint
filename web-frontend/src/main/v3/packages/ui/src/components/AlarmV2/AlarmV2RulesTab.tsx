import React from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { MdOutlineAdd } from 'react-icons/md';
import { AlarmV2RuleList } from './AlarmV2RuleList';
import { AlarmV2TableSearch } from './AlarmV2TableSearch';
import { AlarmV2Rule, ApplicationType } from '@pinpoint-fe/ui/src/constants/types';

/**
 * The rules tab. A rule belongs to an application, so this is the only tab that knows
 * which one is selected, and the picker sits here with it.
 */
export interface AlarmV2RulesTabProps {
  hasPermission: boolean;
  selectedApplication?: ApplicationType;
  applicationPicker: React.ReactNode;
  hasApplicableTemplate: boolean;
  onAddRule: () => void;
  onAddFromTemplate: () => void;
  onOpenRule: (rule: AlarmV2Rule.RuleData) => void;
  onDeleteRule: (rule: AlarmV2Rule.RuleData) => void;
  onOpenHistory: (rule: AlarmV2Rule.RuleData) => void;
  onUnlinkTemplate: (templateId: number, templateName: string, ruleCount: number) => void;
  onToggleEnabled: (rule: AlarmV2Rule.RuleData, enabled: boolean) => void;
}

export const AlarmV2RulesTab = ({
  hasPermission,
  selectedApplication,
  applicationPicker,
  hasApplicableTemplate,
  onAddRule,
  onAddFromTemplate,
  onOpenRule,
  onDeleteRule,
  onOpenHistory,
  onUnlinkTemplate,
  onToggleEnabled,
}: AlarmV2RulesTabProps) => {
  const { t } = useTranslation();
  const [query, setQuery] = React.useState('');
  const applicationName = selectedApplication?.applicationName;

  return (
    <div className="space-y-3">
      {applicationPicker}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <AlarmV2TableSearch
          value={query}
          placeholder={t('CONFIGURATION.ALARM_V2.SEARCH_RULES')}
          onChange={setQuery}
        />
        <div className="flex flex-wrap gap-2">
          <Button disabled={!applicationName || !hasPermission} onClick={onAddRule}>
            <MdOutlineAdd className="mr-1" />
            {t('CONFIGURATION.ALARM_V2.ADD_RULE')}
          </Button>
          <Button
            variant="outline"
            disabled={!applicationName || !hasPermission || !hasApplicableTemplate}
            onClick={onAddFromTemplate}
          >
            <MdOutlineAdd className="mr-1" />
            {t('CONFIGURATION.ALARM_V2.ADD_FROM_TEMPLATE')}
          </Button>
        </div>
      </div>
      <AlarmV2RuleList
        applicationName={applicationName}
        applicationType={selectedApplication?.serviceType}
        rowFilterInfo={{ query }}
        disabled={!hasPermission}
        onClickRowItem={onOpenRule}
        onClickEdit={onOpenRule}
        onClickDelete={onDeleteRule}
        onClickHistory={onOpenHistory}
        onUnlinkTemplate={onUnlinkTemplate}
        onToggleEnabled={onToggleEnabled}
      />
    </div>
  );
};
