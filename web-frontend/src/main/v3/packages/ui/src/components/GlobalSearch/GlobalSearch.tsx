import { globalSearchDisplayAtom } from '@pinpoint-fe/ui/src/atoms';
import { MenuItemType as MenuItem } from '@pinpoint-fe/ui/src/constants';
('@radix-ui/react-icons');
import Fuse from 'fuse.js';
import {
  Command,
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
  CommandShortcut,
} from '../../';
import { useAtom } from 'jotai';
import React from 'react';
import { createPortal } from 'react-dom';
import { useNavigate } from 'react-router-dom';
import { getTransactionDetailPathByTransactionId, isRegexString } from '@pinpoint-fe/ui/src/utils';
import { PiStack } from 'react-icons/pi';

export interface GlobalSearchProps {
  services?: MenuItem[];
}

export const GlobalSearch = ({ services }: GlobalSearchProps) => {
  const transactionIdRegExp = new RegExp(/^[a-zA-Z0-9-_.]+\^\d+\^\d+$/);
  const navigate = useNavigate();
  const portalTargetElement = document.getElementById('__pinpoint_global_search__');
  const [open, setOpen] = useAtom(globalSearchDisplayAtom);
  const [search, setSearch] = React.useState('');

  React.useEffect(() => {
    if (portalTargetElement) {
      const down = (e: KeyboardEvent) => {
        if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
          setOpen((open) => !open);
        }
        if (e.code === 'Escape' && open) {
          setOpen(false);
        }
      };
      document.addEventListener('keydown', down);
      return () => document.removeEventListener('keydown', down);
    } else {
      console.warn('Please register the target element for global search portal in root html');
    }
  }, []);

  const isTransactionId = (tid: string) => {
    return transactionIdRegExp.test(tid.trim());
  };

  const isValueFuzzyMatch = (value: string, search: string) => {
    const fuzzySearch = new Fuse([value], {
      threshold: 0.3,
    });
    return fuzzySearch.search(search).length > 0 ? true : false;
  };

  return (
    portalTargetElement &&
    createPortal(
      <CommandDialog
        open={open}
        onOpenChange={setOpen}
        dialogClassName="max-w-[40rem] data-[state=open]:!slide-in-from-top-[0%] data-[state=closed]:!slide-out-to-top-[0%] top-[15%] translate-y-0"
      >
        <Command
          loop
          filter={(value, search, keywords) => {
            if (search) {
              const isValueMatch = isValueFuzzyMatch(value, search);

              if (keywords?.length) {
                const isKeywordFromRegExpMatch = keywords.some((keyword) => {
                  const isKeyordFromRegExp = isRegexString(keyword);
                  if (isKeyordFromRegExp) {
                    return new RegExp(keyword.slice(1, -1)).test(search);
                  } else {
                    const extendValue = value + ' ' + keywords.join(' ');
                    return isValueFuzzyMatch(extendValue, search);
                  }
                });

                return Number(isValueMatch || isKeywordFromRegExpMatch);
              } else {
                return Number(isValueMatch);
              }
            }
            return 0;
          }}
        >
          <CommandInput
            value={search}
            onValueChange={setSearch}
            placeholder="Type a command or search..."
            onKeyDown={(e) => {
              const trimmedInput = e.currentTarget.value.trim();

              if (e.code === 'Enter' && isTransactionId(trimmedInput)) {
                navigate(getTransactionDetailPathByTransactionId(trimmedInput));
                setOpen(false);
              }
            }}
          />
          <CommandList className="max-h-[32rem]">
            <CommandEmpty>No results found.</CommandEmpty>
            <CommandGroup heading="Suggestions">
              <CommandItem
                disabled={!transactionIdRegExp.test(search)}
                className="gap-2"
                keywords={[transactionIdRegExp.toString()]}
                onSelect={() => {
                  navigate(getTransactionDetailPathByTransactionId(search));
                  setOpen(false);
                }}
              >
                <PiStack />
                <span>Search by Transaction Id</span>
                <CommandShortcut>ex) example^1720167721270^12345</CommandShortcut>
              </CommandItem>
            </CommandGroup>
            <CommandSeparator />
            <CommandGroup heading="Services">
              {services?.map((service, i) => {
                const Icon = service.icon;
                if (service.hide) {
                  return null;
                }
                return (
                  <CommandItem
                    key={i}
                    className="gap-2"
                    onSelect={() => {
                      if (service.href) {
                        navigate(service.href);
                        setOpen(false);
                      }
                    }}
                  >
                    {Icon && Icon}
                    <span>{service.name}</span>
                    <CommandShortcut>{service.path}</CommandShortcut>
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
        </Command>
      </CommandDialog>,
      portalTargetElement,
    )
  );
};
