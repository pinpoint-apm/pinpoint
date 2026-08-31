import React from 'react';
import { FaSearch } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import { ServerMapSearchItem } from '@pinpoint-fe/ui/src/utils';
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover';
import { Button } from '../ui/button';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '../ui/command';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '..';

export interface ServerMapSearchListProps {
  list?: ServerMapSearchItem[];
  onClickItem?: (item: ServerMapSearchItem) => void;
  inputPlaceHolder?: string;
}

export const ServerMapSearchList = ({
  list = [],
  onClickItem,
  inputPlaceHolder = 'Input Node Name',
}: ServerMapSearchListProps) => {
  const { t } = useTranslation();
  const [open, setOpen] = React.useState(false);

  const handleClickItem: ServerMapSearchListProps['onClickItem'] = (item) => {
    onClickItem?.(item);
    setOpen(false);
  };

  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <div>
            <Popover open={open} onOpenChange={setOpen}>
              <PopoverTrigger asChild>
                <Button variant="outline" className="flex w-12 h-12 p-2 text-lg">
                  <FaSearch />
                </Button>
              </PopoverTrigger>
              <PopoverContent side={'left'} align="start" className="p-0 w-90">
                <Command>
                  <CommandInput placeholder={inputPlaceHolder} />
                  <CommandList>
                    <CommandEmpty>{t('COMMON.EMPTY_ON_SEARCH')}</CommandEmpty>
                    <CommandGroup>
                      {list.map((item, i) => {
                        const { node, isServiceGroup, serviceName } = item;
                        // service group은 application 묶음이라 serviceType이 없다. flatten이
                        // 자식 첫 노드의 타입을 합성해 두지만 그것을 이름 뒤에 붙이면 B가
                        // TOMCAT application인 것처럼 읽힌다. 이름만 보여준다.
                        const text = isServiceGroup
                          ? node.applicationName
                          : `${node.applicationName} (${node.serviceType})`;
                        // 소속 service를 보여주는 항목은 그 이름까지 값에 담는다. 이름이 같은
                        // application이 다른 service에도 있을 수 있어(cmdk는 value로 항목을
                        // 식별한다) 값이 겹치지 않게 하고, service 이름으로 검색해도 소속
                        // application이 함께 걸리게 하기 위해서다.
                        const value = serviceName ? `${text} ${serviceName}` : text;

                        return (
                          <CommandItem
                            key={`${node.key}-${i}`}
                            value={value}
                            onSelect={() => handleClickItem(item)}
                          >
                            <div className="flex-1 min-w-0 truncate" title={text}>
                              {text}
                            </div>
                            {serviceName && (
                              // 긴 service 이름이 application 이름을 밀어내지 않도록 폭을 제한한다.
                              // shrink-0 없이 두면 이름이 긴 쪽이 행을 다 차지한다(왼쪽은 basis 0).
                              <div
                                className="max-w-[45%] pl-2 text-xs truncate shrink-0 text-muted-foreground"
                                title={serviceName}
                              >
                                {serviceName}
                              </div>
                            )}
                          </CommandItem>
                        );
                      })}
                    </CommandGroup>
                  </CommandList>
                </Command>
              </PopoverContent>
            </Popover>
          </div>
        </TooltipTrigger>
        <TooltipContent side="left">
          <p>Search Node</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
};
