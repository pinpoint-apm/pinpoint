import React from 'react';
import { FaSearch } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import { ServerMapSearchItem, serverMapSearchFilter } from '@pinpoint-fe/ui/src/utils';
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
                <Command filter={serverMapSearchFilter}>
                  <CommandInput placeholder={inputPlaceHolder} />
                  <CommandList>
                    <CommandEmpty>{t('COMMON.EMPTY_ON_SEARCH')}</CommandEmpty>
                    <CommandGroup>
                      {list.map((item, i) => {
                        const { node, isServiceGroup, serviceName, callee } = item;
                        // service group은 application 묶음이라 serviceType이 없다. flatten이
                        // 자식 첫 노드의 타입을 합성해 두지만 그것을 이름 뒤에 붙이면 B가
                        // TOMCAT application인 것처럼 읽힌다. 이름만 보여준다.
                        // USER 노드는 이름이 모두 `USER`라 호출하는 application으로 구별한다.
                        // 앞부분은 다른 노드와 같은 `이름 (타입)` 형식을 그대로 두고 뒤에 덧붙인다.
                        const nodeText = `${node.applicationName} (${node.serviceType})`;
                        const text = isServiceGroup
                          ? node.applicationName
                          : callee
                            ? `${nodeText} → ${callee.applicationName} (${callee.serviceType})`
                            : nodeText;
                        // cmdk는 value로 항목을 식별하므로 노드마다 하나뿐인 key를 쓴다. 보이는
                        // 글자를 value로 두면 USER처럼 같은 글자의 항목이 여럿일 때 화살표 키
                        // 강조와 Enter 선택이 엉킨다. 검색은 keywords로 한다 — 소속 service로
                        // 검색해도 소속 application이 걸리고, application 이름으로 검색하면
                        // 그 앞의 USER도 함께 걸린다.
                        const keywords = [text, serviceName].filter((k): k is string => !!k);

                        return (
                          <CommandItem
                            key={`${node.key}-${i}`}
                            value={node.key}
                            keywords={keywords}
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
