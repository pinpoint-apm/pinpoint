import { ColumnDef, Row } from '@tanstack/react-table';
import { cn } from '../../../../lib';
import { DataTable } from '../../../DataTable';

export interface ApplicationDataSourceChartTableProps {
  data?: ApplicationDataSourceChartTableData[];
  className?: string;
  onClickRow?: (data: Row<ApplicationDataSourceChartTableData>) => void;
  selectedRowIndex?: number;
}

export type ApplicationDataSourceChartTableData = {
  serviceTypeCode: string;
  jdbcUrl: string;
};

export const ApplicationDataSourceChartTable = ({
  data = [],
  className,
  onClickRow,
  selectedRowIndex = 0,
}: ApplicationDataSourceChartTableProps) => {
  return (
    <div className={cn('w-full px-2', className)}>
      <DataTable
        tableClassName="border text-xs"
        columns={columns}
        data={data || []}
        onClickRow={onClickRow}
        rowSelectionInfo={{ [selectedRowIndex]: true }}
      />
    </div>
  );
};

const columns: ColumnDef<ApplicationDataSourceChartTableData>[] = [
  {
    accessorKey: 'jdbcUrl',
    header: 'Jdbc URL',
  },
  {
    accessorKey: 'serviceTypeCode',
    header: 'ServiceType Code',
  },
];
