import { AlarmRule } from '@pinpoint-fe/ui/src/constants';
import { DataTable } from '../../components/DataTable';
import { AlarmTableColumns, alarmTableColumns } from './alarmTableColumns';
import { cn } from '../../lib/utils';

export interface AlarmTableProps extends AlarmTableColumns {
  data?: AlarmRule.Response;
  onClickRowItem?: (data: AlarmRule.AlarmRuleData) => void;
}

export const AlarmTable = ({ data, onClickRowItem, ...props }: AlarmTableProps) => {
  const columns = alarmTableColumns(props);
  return (
    <div className={cn('rounded-md border')}>
      <DataTable
        autoResize
        columns={columns}
        data={data || []}
        emptyMessage={data?.length === 0 ? undefined : 'Select your application first.'}
        onClickRow={(data) => {
          onClickRowItem?.(data.original);
        }}
      />
    </div>
  );
};
