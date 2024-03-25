import React from 'react';
import { FaSearch } from 'react-icons/fa';
import { FilteredMap, GetServerMap } from '@pinpoint-fe/constants';
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover';
import { Button } from '../ui/button';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem } from '../ui/command';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '..';

export interface ServerMapSearchListProps {
  list?: GetServerMap.NodeData[] | FilteredMap.NodeData[];
  onClickItem?: (nodeData: GetServerMap.NodeData | FilteredMap.NodeData) => void;
  inputPlaceHolder?: string;
}

export const ServerMapSearchList = ({
  list = [],
  onClickItem,
  inputPlaceHolder = 'Input Node Name',
}: ServerMapSearchListProps) => {
  const [open, setOpen] = React.useState(false);

  const handleClickItem: ServerMapSearchListProps['onClickItem'] = (node) => {
    onClickItem?.(node);
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger>
              <Button variant="outline" className="flex w-12 h-12 p-2 text-lg">
                <FaSearch />
              </Button>
            </TooltipTrigger>
            <TooltipContent side="left">
              <p>Search Node</p>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </PopoverTrigger>
      <PopoverContent side={'left'} align="start" className="p-0 w-90">
        <Command>
          <CommandInput placeholder={inputPlaceHolder} />
          <CommandEmpty>No framework found.</CommandEmpty>
          <CommandGroup className="overflow-auto max-h-80">
            {list.map((l, i) => {
              const text = `${l.applicationName} (${l.serviceType})`;

              return (
                <CommandItem key={i} value={text} onSelect={() => handleClickItem(l)}>
                  <div className="truncate">{text}</div>
                </CommandItem>
              );
            })}
          </CommandGroup>
        </Command>
      </PopoverContent>
    </Popover>
  );
};
