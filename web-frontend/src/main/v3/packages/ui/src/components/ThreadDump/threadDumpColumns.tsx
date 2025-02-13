import { ActiveThreadLightDump } from '@pinpoint-fe/ui/src/constants';
import { ColumnDef } from '@tanstack/react-table';
import { format } from 'date-fns';

export const transactionListTableColumns =
  (): ColumnDef<ActiveThreadLightDump.ThreadDumpData>[] => [
    {
      accessorKey: 'index',
      header: '#',
      size: 30,
      cell: (props) => {
        return props.row.index + 1;
      },
      meta: {
        headerClassName: 'grow-0',
        cellClassName: 'grow-0 text-center',
      },
    },
    {
      accessorKey: 'threadId',
      header: 'ID',
      size: 60,
      cell: (props) => {
        return props.getValue();
      },
      meta: {
        headerClassName: 'grow-0',
        cellClassName: 'grow-0',
      },
    },
    {
      accessorKey: 'threadName',
      header: 'Name',
      size: 200,
      cell: (props) => {
        return props.getValue();
      },
    },
    {
      accessorKey: 'threadState',
      header: 'State',
      size: 100,
      cell: (props) => {
        return props.getValue();
      },
      meta: {
        headerClassName: 'grow-0',
        cellClassName: 'grow-0',
      },
    },
    {
      accessorKey: 'startTime',
      header: 'StartTime',
      size: 160,
      cell: (props) => {
        const timestamp = props.getValue() as number;

        return timestamp ? format(timestamp, 'YYY.MM.dd HH:mm:ss SSS') : '';
      },
      meta: {
        headerClassName: 'grow-0',
        cellClassName: 'grow-0 text-right',
      },
    },
    {
      accessorKey: 'execTime',
      header: 'Exec(ms)',
      size: 80,
      cell: (props) => {
        return props.getValue();
      },
      meta: {
        cellClassName: 'grow-0 text-right',
        headerClassName: 'grow-0',
      },
    },
    {
      accessorKey: 'sampled',
      header: 'Sampled',
      size: 80,
      cell: (props) => {
        return `${props.getValue()}`;
      },
      meta: {
        headerClassName: 'grow-0',
        cellClassName: 'grow-0',
      },
    },
    {
      accessorKey: 'entryPoint',
      header: 'Path',
      size: 90,
      cell: (props) => {
        return props.getValue();
      },
    },
    {
      accessorKey: 'transactionId',
      header: 'Transaction ID',
      size: 100,
      cell: (props) => {
        return props.getValue();
      },
    },
  ];
