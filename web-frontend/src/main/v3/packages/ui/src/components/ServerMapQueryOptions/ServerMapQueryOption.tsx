import React from 'react';
import { FaSignInAlt, FaSignOutAlt, FaMapSigns } from 'react-icons/fa';
import { useUpdateEffect } from 'usehooks-ts';
import { Popover, PopoverClose, PopoverContent, PopoverTrigger } from '../ui/popover';
import { Button } from '../ui/button';
import { Separator } from '../ui/separator';
import { Checkbox } from '../ui/checkbox';
import { ButtonGroup, ButtonGroupItem } from '../ui/button-group';
import { cn } from '../../lib/utils';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '..';

export interface ServerMapQueryOption {
  inbound?: number;
  outbound?: number;
  wasOnly?: boolean;
  bidirectional?: boolean;
}

export interface ServerMapQueryOptionProps {
  queryOption?: ServerMapQueryOption;
  onApply?: (pamas?: ServerMapQueryOption) => void;
}

const boundCount = [1, 2, 3, 4];

export const ServerMapQueryOption = ({ queryOption, onApply }: ServerMapQueryOptionProps) => {
  const [open, setOpen] = React.useState(false);
  const [inbound, setInbound] = React.useState(queryOption?.inbound || 1);
  const [outbound, setOutbound] = React.useState(queryOption?.outbound || 1);
  const [wasOnly, setWasOnly] = React.useState(queryOption?.wasOnly || false);
  const [bidirectional, setBidirectional] = React.useState(queryOption?.bidirectional || false);

  const setOption = (queryOption?: ServerMapQueryOption) => {
    setInbound(queryOption?.inbound || 1);
    setOutbound(queryOption?.outbound || 1);
    setWasOnly(queryOption?.wasOnly || false);
    setBidirectional(queryOption?.bidirectional || false);
  };

  useUpdateEffect(() => {
    setOption(queryOption);
  }, [queryOption]);

  return (
    <Popover
      open={open}
      onOpenChange={(open) => {
        setOpen(open);

        if (!open) {
          setOption(queryOption);
        }
      }}
    >
      <PopoverTrigger>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger>
              <Button
                variant="outline"
                className="flex p-2 text-gray-400 h-100 hover:text-gray-500"
                asChild
              >
                <div className="flex flex-col w-12 text-xs gap-0.5 ">
                  <div
                    className={cn('flex flex-col items-center justify-center h-10', {
                      '!text-primary font-semibold': wasOnly,
                    })}
                  >
                    <div className="tracking-wider">APP</div>
                    ONLY
                  </div>
                  <Separator />
                  <div
                    className={cn('flex items-center justify-center h-10 text-lg', {
                      '!text-primary': bidirectional,
                    })}
                  >
                    <FaMapSigns />
                  </div>
                  <Separator />
                  <div
                    className={cn('flex items-center justify-center h-10 gap-1', {
                      '!text-foreground font-semibold': inbound > 1,
                    })}
                  >
                    <span className={cn({ '!text-emerald-400': inbound > 1 })}>
                      <FaSignInAlt />
                    </span>
                    {inbound}
                  </div>
                  <Separator />
                  <div
                    className={cn('flex items-center justify-center h-10 gap-1', {
                      '!text-foreground font-semibold': outbound > 1,
                    })}
                  >
                    <span className={cn({ '!text-emerald-400': outbound > 1 })}>
                      <FaSignOutAlt />
                    </span>
                    {outbound}
                  </div>
                </div>
              </Button>
            </TooltipTrigger>
            <TooltipContent side="left">
              <p>Server-map Query Option</p>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </PopoverTrigger>
      <PopoverContent side={'left'} align="start" className="w-90">
        <div>
          <div className="mb-1 font-semibold">Server-map Query Option</div>
          <div className="text-sm font-light text-gray-400">
            Set the query options for the Server-map
          </div>
          <Separator className="mt-2 mb-3" />
          <div className="grid grid-cols-[120px_auto] text-sm mb-10 h-40 items-center [&>*:nth-child(odd)]:text-right [&>*:nth-child(even)]:ml-5">
            <div>Application Only</div>
            <Checkbox
              checked={wasOnly}
              onCheckedChange={(checked) => {
                setWasOnly(checked as boolean);
              }}
            />
            <div>Bidirectional</div>
            <Checkbox
              checked={bidirectional}
              onCheckedChange={(checked) => {
                setBidirectional(checked as boolean);
              }}
            />
            <div>Inbound</div>
            <ButtonGroup
              defaultValue={`${inbound}`}
              className="h-7"
              onValueChange={(value) => {
                setInbound(parseInt(value, 10));
              }}
            >
              {boundCount.map((count, i) => (
                <ButtonGroupItem key={i} className="h-6" value={`${count}`}>
                  {count}
                </ButtonGroupItem>
              ))}
            </ButtonGroup>
            <div>Outbound</div>
            <ButtonGroup
              className="h-7"
              defaultValue={`${outbound}`}
              onValueChange={(value) => {
                setOutbound(parseInt(value, 10));
              }}
            >
              {boundCount.map((count, i) => (
                <ButtonGroupItem key={i} className="h-6" value={`${count}`}>
                  {count}
                </ButtonGroupItem>
              ))}
            </ButtonGroup>
          </div>
          <div className="flex justify-end gap-2 ">
            <PopoverClose>
              <Button className="w-20 font-normal" variant="outline">
                Cancel
              </Button>
            </PopoverClose>
            <PopoverClose>
              <Button
                className="w-20 font-normal"
                onClick={() => {
                  setTimeout(() => {
                    onApply?.({
                      inbound,
                      outbound,
                      wasOnly,
                      bidirectional,
                    });
                  }, 100);
                }}
              >
                Apply
              </Button>
            </PopoverClose>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};
