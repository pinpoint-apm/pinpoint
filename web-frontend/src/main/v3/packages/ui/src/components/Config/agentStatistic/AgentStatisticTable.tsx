import React from 'react';
import {
  APP_PATH,
  BASE_PATH,
  SEARCH_PARAMETER_DATE_FORMAT,
  SearchApplication,
} from '@pinpoint-fe/ui/src/constants';
import { Button, Input, VirtualizedDataTable } from '../../../components';
import { Cell, ColumnDef } from '@tanstack/react-table';
import { LuChevronRight, LuChevronDown, LuMoveDown, LuMoveUp } from 'react-icons/lu';
import { cn } from '../../../lib';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { addMinutes, format } from 'date-fns';

type ExpandableDataType = {
  applicationName: string;
  subRows?: SearchApplication.Instance[];
};

function containsString(obj: SearchApplication.Instance, input: string): boolean {
  const keysToCheck: (keyof SearchApplication.Instance)[] = [
    'applicationName',
    'agentId',
    'agentName',
    'agentVersion',
    'vmVersion',
  ];
  return keysToCheck.some(
    (key) => typeof obj[key] === 'string' && obj[key]?.toLocaleLowerCase()?.includes(input),
  );
}

export function AgentStatisticTable({ data }: { data?: SearchApplication.Application[] }) {
  const [input, setInput] = React.useState('');
  const [filterInput, setFilterInput] = React.useState('');
  const [filteredRowIds, setFilteredRowIds] = React.useState<string[]>([]);
  const [focusRowId, setFocusRowId] = React.useState<string>();

  const expandableData = React.useMemo(() => {
    return (
      data?.map((d) => {
        return {
          applicationName: d?.groupName,
          subRows: d?.instancesList,
        };
      }) || []
    );
  }, [data]);

  // flattenedData to check index of focusRowId
  const flattenedData = React.useMemo(() => {
    return data?.reduce(
      (acc, item, i) => {
        // 현재 항목의 name 추가
        acc.push({ applicationName: item?.groupName, id: `${i}` });

        if (Array.isArray(item?.instancesList)) {
          acc.push(
            ...item.instancesList.map((instance, j) => {
              return {
                ...(instance || {}),
                id: `${i}.${j}`,
              };
            }),
          );
        }

        return acc;
      },
      [] as (
        | { applicationName: string; id: string }
        | (SearchApplication.Instance & { id: string })
      )[],
    );
  }, [data]);

  const columns: ColumnDef<ExpandableDataType>[] = [
    {
      accessorKey: 'applicationName',
      header: 'ApplicationName',
      cell: (props) => {
        return (
          <div className="flex items-center">
            {props.row.getCanExpand() && (
              <button
                onClick={props.row.getToggleExpandedHandler()}
                className="pr-1 cursor-pointer"
              >
                {props.row.getIsExpanded() ? <LuChevronDown /> : <LuChevronRight />}
              </button>
            )}
            <span
              className={cn({
                'font-bold': props.row.getCanExpand(),
              })}
            >
              {props?.getValue() as string}
            </span>
          </div>
        );
      },
      size: 350,
    },
    {
      accessorKey: 'agentId',
      header: 'Agent ID',
    },
    {
      accessorKey: 'agentName',
      header: 'Agent Name',
    },
    {
      accessorKey: 'agentVersion',
      header: 'Agent Version',
    },
    {
      accessorKey: 'vmVersion',
      header: 'JVM Version',
    },
  ];

  React.useEffect(() => {
    if (!filterInput) {
      setFilteredRowIds([]);
      return;
    }

    const newFocusRows: string[] = [];
    expandableData?.forEach((group, i) => {
      group?.subRows?.forEach((sRow, j) => {
        if (containsString(sRow, filterInput)) {
          newFocusRows.push(`${i}.${j}`);
        }
      });
    });
    setFilteredRowIds(newFocusRows);
    setFocusRowId(newFocusRows?.[0]);
  }, [filterInput]);

  function goToNextSearchIndex() {
    setFocusRowId((prev) => {
      const idIndex = filteredRowIds?.findIndex((fw) => fw === prev);
      return filteredRowIds?.[idIndex === filteredRowIds?.length - 1 ? 0 : idIndex + 1];
    });
  }

  function backToPrevSearchIndex() {
    setFocusRowId((prev) => {
      const idIndex = filteredRowIds?.findIndex((fw) => fw === prev);
      return filteredRowIds?.[idIndex === 0 ? filteredRowIds?.length - 1 : idIndex - 1];
    });
  }

  function handleClickCell(
    data: Cell<ExpandableDataType | SearchApplication.Instance, string | number>,
  ) {
    const original = data?.row?.original as SearchApplication.Instance;

    if ((original as ExpandableDataType)?.subRows?.length) {
      return;
    }

    if (data?.column?.id === 'applicationName') {
      window.open(
        `${BASE_PATH}${APP_PATH.SERVER_MAP}/${original?.applicationName}@${original?.serviceType}`,
      );
    } else {
      const endTime = format(addMinutes(original?.startTimestamp, 5), SEARCH_PARAMETER_DATE_FORMAT);
      window.open(
        `${BASE_PATH}${APP_PATH.INSPECTOR}/${original?.applicationName}@${original?.serviceType}?from=${format(original?.startTimestamp, SEARCH_PARAMETER_DATE_FORMAT)}&to=${endTime}&agentId=${original?.agentId}`,
      );
    }
  }

  return (
    <div className="flex flex-col gap-2 overflow-hidden h-[-webkit-fill-available]">
      <div className="border flex rounded pr-0.5 w-64 self-end">
        <Input
          className="h-full text-xs border-none shadow-none focus-visible:ring-0 placeholder:text-xs"
          placeholder="Search..."
          value={input}
          onChange={(e) => setInput(e.currentTarget.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              if (e.shiftKey) {
                backToPrevSearchIndex();
              } else {
                setFilterInput((prev) => {
                  if (prev === input) {
                    goToNextSearchIndex();
                  }
                  return input;
                });
              }
            } else if (e.key === 'Escape') {
              setInput('');
              setFilterInput('');
              setFocusRowId(undefined);
            }
          }}
        />
        <div className="flex items-center opacity-50">
          {filteredRowIds?.length ? (
            <>
              <span className="whitespace-nowrap text-xxs">
                {filteredRowIds?.findIndex((id) => id === focusRowId) + 1} of{' '}
                {filteredRowIds?.length}
              </span>
              <Button
                variant="ghost"
                className="h-full p-0.5"
                onClick={() => backToPrevSearchIndex()}
              >
                <LuMoveUp />
              </Button>
              <Button
                variant="ghost"
                className="h-full p-0.5"
                onClick={() => goToNextSearchIndex()}
              >
                <LuMoveDown />
              </Button>
            </>
          ) : (
            ''
          )}
          <Button variant="ghost" className="h-full p-0.5" onClick={() => setFilterInput(input)}>
            <RxMagnifyingGlass />
          </Button>
        </div>
      </div>
      <VirtualizedDataTable<ExpandableDataType, string | number>
        enableColumnResizing
        tableClassName={cn('text-xs [&_td]:p-1.5')}
        rowClassName={(row) => {
          if (row?.id === focusRowId) {
            return 'bg-yellow-200';
          }

          if (filteredRowIds?.includes(row?.id)) {
            return 'bg-yellow-100';
          }
          return '';
        }}
        focusRowIndex={flattenedData?.findIndex(
          (fd: { id: string | undefined }) => fd?.id === focusRowId,
        )}
        data={expandableData || []}
        columns={columns || []}
        onClickCell={handleClickCell}
      />
    </div>
  );
}
