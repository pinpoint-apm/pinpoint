import React from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@pinpoint-fe/ui/src/components/ui/button';
import { MdOutlineAdd } from 'react-icons/md';
import { AlarmV2ChannelList } from './AlarmV2ChannelList';
import { AlarmV2TableSearch } from './AlarmV2TableSearch';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';

/** The channels tab. A channel belongs to the service, so no application reaches it. */
export const AlarmV2ChannelsTab = ({
  hasPermission,
  onAddChannel,
  onOpenChannel,
  onDeleteChannel,
}: {
  hasPermission: boolean;
  onAddChannel: () => void;
  onOpenChannel: (channel: AlarmV2Channel.ChannelData) => void;
  onDeleteChannel: (channel: AlarmV2Channel.ChannelData) => void;
}) => {
  const { t } = useTranslation();
  const [query, setQuery] = React.useState('');

  return (
    <div className="space-y-3">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <AlarmV2TableSearch
          value={query}
          placeholder={t('CONFIGURATION.ALARM_V2.SEARCH_CHANNELS')}
          onChange={setQuery}
        />
        <div className="flex flex-wrap gap-2">
          <Button disabled={!hasPermission} onClick={onAddChannel}>
            <MdOutlineAdd className="mr-1" />
            {t('CONFIGURATION.ALARM_V2.ADD_CHANNEL')}
          </Button>
        </div>
      </div>
      <AlarmV2ChannelList
        rowFilterInfo={{ query }}
        disabled={!hasPermission}
        onClickRowItem={onOpenChannel}
        onClickEdit={onOpenChannel}
        onClickDelete={onDeleteChannel}
      />
    </div>
  );
};
