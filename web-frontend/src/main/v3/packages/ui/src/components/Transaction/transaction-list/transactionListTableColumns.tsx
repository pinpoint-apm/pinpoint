import { ApplicationType, BASE_PATH, Transaction } from '@pinpoint-fe/ui/src/constants';
import { ColumnDef } from '@tanstack/react-table';
import { format } from 'date-fns';
import { FaFire } from 'react-icons/fa';
import { Button } from '../../../components';
import { RxExternalLink } from 'react-icons/rx';
import {
  getTransactionDetailPath,
  getTransactionDetailQueryString,
} from '@pinpoint-fe/ui/src/utils';

export const transactionListTableColumns = (
  application: ApplicationType | null,
): ColumnDef<Transaction>[] => [
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
    accessorKey: 'startTime',
    header: 'StartTime',
    size: 200,
    cell: (props) => {
      const timestamp = props.getValue() as number;
      return format(timestamp, 'yyyy.MM.dd HH:mm:ss SSS');
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0',
    },
  },
  {
    accessorKey: 'application',
    header: 'Path',
    size: 250,
    cell: (props) => {
      const data = props.row.original;

      return (
        <div className="flex items-center">
          <Button
            className="text-muted-foreground p-0 w-4 h-4 mr-1.5"
            variant="ghost"
            onClick={(e) => {
              e.stopPropagation();
              window.open(
                `${BASE_PATH}${getTransactionDetailPath(
                  application,
                )}?${getTransactionDetailQueryString({
                  agentId: data.agentId,
                  spanId: data.spanId,
                  traceId: data.traceId,
                  focusTimestamp: data.collectorAcceptTime,
                })}`,
              );
            }}
          >
            <RxExternalLink />
          </Button>
          <div className="truncate">{props.getValue() as string}</div>
        </div>
      );
    },
  },
  {
    accessorKey: 'endpoint',
    header: 'EndPoint',
    size: 210,
    cell: (props) => {
      return props.getValue();
    },
  },
  {
    accessorKey: 'elapsed',
    header: 'Res(ms)',
    size: 80,
    cell: (props) => {
      return props.getValue();
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0 text-right',
    },
  },
  {
    accessorKey: 'exception',
    header: 'Exception',
    size: 80,
    cell: (props) => {
      // 0 or 1
      return props.getValue() === 0 ? '' : <FaFire className="fill-status-fail bg-red" />;
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0 text-center',
    },
  },
  {
    accessorKey: 'agentId',
    header: 'Agent Id',
    size: 90,
    cell: (props) => {
      return props.getValue();
    },
  },
  {
    accessorKey: 'remoteAddr',
    header: 'Client IP',
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
    accessorKey: 'traceId',
    header: 'Transaction',
    size: 220,
    cell: (props) => {
      return props.getValue();
    },
  },
  {
    accessorKey: 'agentName',
    header: 'Agent Name',
    // size: 100,
    cell: (props) => {
      return props.getValue();
    },
  },
];
