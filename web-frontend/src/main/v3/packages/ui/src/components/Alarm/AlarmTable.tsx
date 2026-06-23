import { useTranslation } from 'react-i18next';
import { AlarmRule } from '@pinpoint-fe/ui/src/constants';
import { DataTable } from '../../components/DataTable';
import { AlarmTableColumns, alarmTableColumns } from './alarmTableColumns';
import { cn } from '../../lib/utils';

export interface AlarmTableProps extends AlarmTableColumns {
  data?: AlarmRule.Response;
  onClickRowItem?: (data: AlarmRule.AlarmRuleData) => void;
}

export const AlarmTable = ({ data, onClickRowItem, ...props }: AlarmTableProps) => {
  const { t } = useTranslation();
  const columns = alarmTableColumns(props, t);
  return (
    <div className={cn('rounded-md border')}>
      <DataTable
        autoResize
        columns={columns}
        data={data || []}
        emptyMessage={
          data?.length === 0 ? undefined : t('CONFIGURATION.COMMON.SELECT_APPLICATION_FIRST')
        }
        onClickRow={(data) => {
          onClickRowItem?.(data.original);
        }}
      />
    </div>
  );
};
