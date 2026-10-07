import React from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { MdOutlineAdd } from 'react-icons/md';
import { AlarmV2TemplateList } from './AlarmV2TemplateList';
import { AlarmV2TableSearch } from './AlarmV2TableSearch';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';

/** The templates tab. A template belongs to the service, so no application reaches it. */
export const AlarmV2TemplatesTab = ({
  hasPermission,
  onAddTemplate,
  onOpenTemplate,
  onDeleteTemplate,
}: {
  hasPermission: boolean;
  onAddTemplate: () => void;
  onOpenTemplate: (template: AlarmV2Template.TemplateData) => void;
  onDeleteTemplate: (template: AlarmV2Template.TemplateData) => void;
}) => {
  const { t } = useTranslation();
  const [query, setQuery] = React.useState('');

  return (
    <div className="space-y-3">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <AlarmV2TableSearch
          value={query}
          placeholder={t('CONFIGURATION.ALARM_V2.SEARCH_TEMPLATES')}
          onChange={setQuery}
        />
        <div className="flex flex-wrap gap-2">
          <Button disabled={!hasPermission} onClick={onAddTemplate}>
            <MdOutlineAdd className="mr-1" />
            {t('CONFIGURATION.ALARM_V2.ADD_TEMPLATE')}
          </Button>
        </div>
      </div>
      <AlarmV2TemplateList
        rowFilterInfo={{ query }}
        disabled={!hasPermission}
        onClickRowItem={onOpenTemplate}
        onClickEdit={onOpenTemplate}
        onClickDelete={onDeleteTemplate}
      />
    </div>
  );
};
