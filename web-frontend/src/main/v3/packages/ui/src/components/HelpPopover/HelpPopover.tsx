import React from 'react';
import { Popover, PopoverContent, PopoverTrigger } from '../../components/ui/popover';
import { MdHelp } from 'react-icons/md';
import * as PopoverPrimitive from '@radix-ui/react-popover';

export const HelpPopover = ({
  title,
  content,
}: {
  title?: React.ReactNode;
  content?: React.ReactNode;
}) => {
  return (
    <Popover>
      <PopoverTrigger>
        <MdHelp />
      </PopoverTrigger>
      <PopoverPrimitive.Portal>
        <PopoverContent className="z-[9999]">
          {title && <h4 className="mb-4 font-medium leading-none">{title}</h4>}
          {content}
        </PopoverContent>
      </PopoverPrimitive.Portal>
    </Popover>
  );
};
