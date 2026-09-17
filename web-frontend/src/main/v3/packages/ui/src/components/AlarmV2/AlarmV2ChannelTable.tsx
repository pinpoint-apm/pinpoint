import React from 'react';
import { useTranslation } from 'react-i18next';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { DataTable, type RowFilterInfo } from '@pinpoint-fe/ui/src/components/DataTable';
import { TABLE_MIN_WIDTH } from './tableStyles';
import { alarmV2ChannelTableColumns } from './alarmV2ChannelTableColumns';

export interface AlarmV2ChannelTableProps {
  data?: AlarmV2Channel.ChannelData[];
  disabled?: boolean;
  rowFilterInfo?: RowFilterInfo;
  onClickRowItem?: (data: AlarmV2Channel.ChannelData) => void;
  onClickEdit?: (data: AlarmV2Channel.ChannelData) => void;
  onClickDelete?: (data: AlarmV2Channel.ChannelData) => void;
}

export const AlarmV2ChannelTable = ({
  data,
  disabled,
  rowFilterInfo,
  onClickRowItem,
  onClickEdit,
  onClickDelete,
}: AlarmV2ChannelTableProps) => {
  const { t } = useTranslation();
  const columns = React.useMemo(
    () => alarmV2ChannelTableColumns({ disabled, onClickEdit, onClickDelete }, t),
    [t, disabled, onClickEdit, onClickDelete],
  );

  return (
    <div className="rounded-md border">
      {/* Same floor as the hand-rolled tables next door: without it DataTable squeezes
          Name to 76px before it starts scrolling, so switching tabs changes how the
          page behaves on a narrow window. */}
      <DataTable
        autoResize
        tableClassName={TABLE_MIN_WIDTH}
        columns={columns}
        data={data || []}
        emptyMessage={t('CONFIGURATION.ALARM_V2.NO_CHANNELS')}
        rowFilterInfo={rowFilterInfo}
        onClickRow={(data) => {
          onClickRowItem?.(data.original);
        }}
      />
    </div>
  );
};
