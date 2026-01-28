import React from 'react';
import { Button, Checkbox, Popover, PopoverContent, PopoverTrigger, Separator } from '../../ui';
import { ColumnDef } from '@tanstack/react-table';
import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { CallTreeTableColumnId } from './callTreeTableColumns';
import { BsGearFill } from 'react-icons/bs';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';

export interface CallTreeTableColumnsSettingProps {
  defaultColumns: ColumnDef<TransactionInfo.CallStackKeyValueMap>[];
  updateColumns: (columnIds: CallTreeTableColumnId[]) => void;
}

export const CallTreeTableColumnsSetting = ({
  defaultColumns,
  updateColumns,
}: CallTreeTableColumnsSettingProps) => {
  const defaultColumnIds: CallTreeTableColumnId[] = [
    'index',
    'title',
    'arguments',
    'begin',
    'gap',
    'elapsedTime',
    'executionPercentage',
    'executionMilliseconds',
    'simpleClassName',
    'apiType',
    'applicationName',
  ];

  const [selectedColumnIds, setSelectedColumnIds] = useLocalStorage<CallTreeTableColumnId[]>(
    APP_SETTING_KEYS.TRANSACTION_CALL_TREE_COLUMNS_SETTING,
    defaultColumnIds,
  );

  React.useEffect(() => {
    updateColumns(selectedColumnIds as CallTreeTableColumnId[]);
  }, [selectedColumnIds]);

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          size="sm"
          variant="outline"
          className="h-7 font-normal gap-1 flex justify-center items-center"
        >
          <BsGearFill className="text-base" size={12} />
          {'Columns setting'}
        </Button>
      </PopoverTrigger>
      <PopoverContent className={'p-4 w-auto'} avoidCollisions align={'start'} side={'bottom'}>
        <div className="text-sm font-medium">Columns setting</div>
        <Separator className="my-2" />
        {defaultColumns.map((col) => {
          if (col.id === 'index') {
            return null;
          }

          return (
            <div key={col.id} className="flex items-center gap-2 px-3 py-1.5 text-xs">
              <Checkbox
                id={col.id as string}
                name={col.id as string}
                checked={selectedColumnIds.includes(col.id as CallTreeTableColumnId)}
                onCheckedChange={(checked) => {
                  if (checked) {
                    setSelectedColumnIds(selectedColumnIds.concat(col.id as CallTreeTableColumnId));
                  } else {
                    setSelectedColumnIds(selectedColumnIds.filter((id) => id !== col.id));
                  }
                }}
              />
              <label htmlFor={col.id} className="truncate cursor-pointer">
                {col.header as string}
              </label>
            </div>
          );
        })}
      </PopoverContent>
    </Popover>
  );
};
