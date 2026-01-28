import {
  BASE_PATH,
  SEARCH_PARAMETER_DATE_FORMAT,
  TransactionInfoType as TransactionInfo,
} from '@pinpoint-fe/ui/src/constants';
import { ColumnDef } from '@tanstack/react-table';
import { formatInTimeZone } from 'date-fns-tz';
import {
  FaFire,
  FaDatabase,
  FaInfoCircle,
  FaPaperPlane,
  FaExchangeAlt,
  FaExclamationTriangle,
} from 'react-icons/fa';
import { LuChevronRight, LuChevronDown } from 'react-icons/lu';
import { Button, ProgressBar } from '../..';
import {
  addCommas,
  convertParamsToQueryString,
  getErrorAnalysisPath,
  getTimezone,
} from '@pinpoint-fe/ui/src/utils';
import { useTimezone, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import React from 'react';

export interface CallTreeTableColumnsProps {
  metaData: TransactionInfo.Response;
  onClickDetailView?: (data: TransactionInfo.CallStackKeyValueMap) => void;
}

export type CallTreeTableColumnId =
  | 'index'
  | 'title'
  | 'arguments'
  | 'begin'
  | 'gap'
  | 'elapsedTime'
  | 'executionPercentage'
  | 'executionMilliseconds'
  | 'simpleClassName'
  | 'apiType'
  | 'agent'
  | 'applicationName'
  | 'agentName';

export const useCallTreeTableColumns = ({
  metaData,
  onClickDetailView,
}: CallTreeTableColumnsProps) => {
  const defaultColumns = React.useMemo(
    () => callTreeTableColumns({ metaData, onClickDetailView }),
    [metaData, onClickDetailView],
  );

  const [columns, setColumns] =
    React.useState<ColumnDef<TransactionInfo.CallStackKeyValueMap>[]>(defaultColumns);

  const updateColumns = React.useCallback(
    (columnIds: CallTreeTableColumnId[]) => {
      setColumns(
        defaultColumns.filter((column) => columnIds.includes(column.id as CallTreeTableColumnId)),
      );
    },
    [defaultColumns],
  );

  return { defaultColumns, columns, updateColumns };
};

export const callTreeTableColumns = ({
  metaData,
  onClickDetailView,
}: CallTreeTableColumnsProps): ColumnDef<TransactionInfo.CallStackKeyValueMap>[] => [
  {
    id: 'index',
    accessorKey: 'index',
    header: '',
    cell: (props) => {
      return (
        <div
          style={{ backgroundColor: calcColor(props.row.original) }}
          className="w-full h-full"
        ></div>
      );
    },
    size: 10,
    meta: {
      cellClassName: '!p-0 grow-0',
      headerClassName: 'px-0.5 grow-0 ',
    },
  },
  {
    id: 'title',
    accessorKey: 'title',
    header: 'Method',
    cell: (props) => {
      const rowData = props.row.original;
      return (
        <div className="flex items-center" style={{ paddingLeft: `${props.row.original.tab}rem` }}>
          {props.row.getCanExpand() && (
            <button onClick={props.row.getToggleExpandedHandler()} className="pr-1 cursor-pointer">
              {props.row.getIsExpanded() ? <LuChevronDown /> : <LuChevronRight />}
            </button>
          )}
          <MethodCell
            {...{
              metaData,
              rowData,
              onClickDetailView,
            }}
          />
        </div>
      );
    },
    size: 350,
  },
  {
    id: 'arguments',
    accessorKey: 'arguments',
    header: 'Arguments',
    cell: (props) => {
      return props.getValue();
    },
  },
  {
    id: 'begin',
    accessorKey: 'begin',
    header: 'StartTime',
    size: 90,
    cell: (props) => {
      const timestamp = props.getValue() as number;
      const timezone = getTimezone();

      return timestamp ? formatInTimeZone(timestamp, timezone, 'HH:mm:ss SSS') : '';
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0',
    },
  },
  {
    id: 'gap',
    accessorKey: 'gap',
    header: 'Gap(ms)',
    size: 65,
    cell: (props) => {
      return props.getValue();
    },
    meta: {
      cellClassName: 'grow-0 text-right',
      headerClassName: 'grow-0',
    },
  },
  {
    id: 'elapsedTime',
    accessorKey: 'elapsedTime',
    header: 'Exec(ms)',
    size: 65,
    cell: (props) => {
      const value = props.getValue();
      if (value) {
        return addCommas(value as string);
      }
      return '';
    },
    meta: {
      cellClassName: 'grow-0 text-right',
      headerClassName: 'grow-0',
    },
  },
  {
    id: 'executionPercentage',
    accessorKey: 'executionPercentage',
    header: 'Exec(%)',
    size: 120,
    cell: (props) => {
      const rowData = props.row.original;
      const entireTime = Number(rowData?.elapsedTime) || 0;
      if (entireTime) {
        const percentage = getExecPercentage(metaData, rowData);

        return (
          <div className="flex items-center w-full h-full">
            <ProgressBar
              className="z-[-1]"
              style={{ width: `${percentage}%` }}
              range={[0, rowData.end - rowData.begin]}
              progress={Number(rowData.executionMilliseconds)}
              tickCount={0}
              hideTick
            />
          </div>
        );
      }
      return null;
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0 text-center',
    },
  },
  {
    id: 'executionMilliseconds',
    accessorKey: 'executionMilliseconds',
    header: 'Self(ms)',
    size: 65,
    cell: (props) => {
      const value = props.getValue();
      if (value) {
        return addCommas(value as string);
      }
      return '';
    },
    meta: {
      cellClassName: 'grow-0 text-right',
      headerClassName: 'grow-0',
    },
  },
  {
    id: 'simpleClassName',
    accessorKey: 'simpleClassName',
    header: 'Class',
    cell: (props) => {
      return props.getValue();
    },
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'apiType',
    accessorKey: 'apiType',
    header: 'API',
    cell: (props) => {
      return props.getValue();
    },
    size: 130,
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'agent',
    accessorKey: 'agent',
    header: 'Agent Id',
    cell: (props) => {
      return props.getValue();
    },
    size: 130,
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'applicationName',
    accessorKey: 'applicationName',
    header: 'Application',
    cell: (props) => {
      return props.getValue();
    },
    size: 130,
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'agentName',
    accessorKey: 'agentName',
    header: 'Agent Name',
    size: 30,
    cell: (props) => {
      return props.getValue();
    },
    meta: {
      // cellClassName: 'max-w-[20px]',
    },
  },
];

const calcColor = (data: TransactionInfo.CallStackKeyValueMap) => {
  let hash = 0;
  let color = '#';
  const agent = data?.agent || data.attributedAgent;

  for (let i = 0; i < agent?.length; i++) {
    hash = agent.charCodeAt(i) + ((hash << 5) - hash);
  }
  for (let i = 0; i < 3; i++) {
    color += ('00' + ((hash >> (i * 8)) & 0xff).toString(16)).slice(-2);
  }

  return color;
};

const MethodCell = (props: {
  metaData: TransactionInfo.Response;
  rowData: TransactionInfo.CallStackKeyValueMap;
  onClickDetailView: CallTreeTableColumnsProps['onClickDetailView'];
}) => {
  const { application, transactionInfo } = useTransactionSearchParameters();
  const { metaData, rowData, onClickDetailView } = props;
  let Icon;
  const text = rowData.title;
  const [timezone] = useTimezone();

  if (rowData.hasException) {
    Icon = <FaFire className="fill-status-fail" />;

    if (rowData.exceptionChainId) {
      let parentIndex = Number(rowData.id);
      while (!metaData.callStack[parentIndex]?.[metaData.callStackIndex.id]) {
        parentIndex--;
      }

      const parentData = metaData.callStack[parentIndex];
      const startTime = parentData[metaData.callStackIndex.begin];
      const endTime = parentData[metaData.callStackIndex.end];
      const baseTime = (startTime + endTime) / 2;
      const from = formatInTimeZone(baseTime - 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
      const to = formatInTimeZone(baseTime + 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
      const href = `${BASE_PATH}${getErrorAnalysisPath(application)}?${convertParamsToQueryString({
        from,
        to,
        transactionInfo: JSON.stringify({
          applicationName: rowData.applicationName,
          agentId: rowData.agent,
          spanId: transactionInfo.spanId,
          transactionId: metaData.transactionId,
          exceptionId: rowData.exceptionChainId,
          timestamp: transactionInfo.focusTimestamp,
          uriTemplate: metaData.uri,
        }),
        openErrorDetail: true,
      })}`;

      return (
        <>
          <div className="flex-none">{Icon}</div>
          <a href={href} target="_blank" className="ml-1 truncate text-primary hover:underline">
            {text}
          </a>
        </>
      );
    }
  } else if (!rowData.isMethod) {
    if (text === 'SQL' || text === 'MONGO-JSON') {
      return (
        <Button
          className="text-xs h-[1rem] gap-0.5 p-1"
          onClick={() => onClickDetailView?.(rowData)}
        >
          <FaDatabase /> {text}
        </Button>
      );
    } else {
      Icon = <FaInfoCircle />;
    }
  } else {
    switch (Number(rowData.methodType)) {
      case 100:
        Icon = <FaPaperPlane />;
        break;
      case 200:
        Icon = <FaExchangeAlt />;
        break;
      case 900:
        Icon = <FaExclamationTriangle />;
        break;
    }
  }
  return (
    <>
      <div className="flex-none">{Icon}</div>
      <div className="ml-1 truncate">{text}</div>
    </>
  );
};

export const getExecPercentage = (
  metaData: TransactionInfo.Response,
  rowData: TransactionInfo.CallStackKeyValueMap,
) => {
  const totalExcuteTime = metaData.callStackEnd - metaData.callStackStart;
  return ((rowData.end - rowData.begin) / totalExcuteTime) * 100;
};
