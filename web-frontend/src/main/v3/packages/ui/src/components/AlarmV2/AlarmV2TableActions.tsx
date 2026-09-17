import React from 'react';
import { useTranslation } from 'react-i18next';
import { RxDotsVertical } from 'react-icons/rx';
import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@pinpoint-fe/ui/src/components/ui';

export interface AlarmV2TableAction<T> {
  label: React.ReactNode;
  disabled?: boolean;
  destructive?: boolean;
  separatorBefore?: boolean;
  onClick?: (data: T) => void;
}

export interface AlarmV2TableActionsProps<T> {
  data: T;
  actions: AlarmV2TableAction<T>[];
}

export const AlarmV2TableActions = <T,>({ data, actions }: AlarmV2TableActionsProps<T>) => {
  const { t } = useTranslation();
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        {/* Ghost, so a row's own actions never outweigh the group header above it. */}
        <Button
          variant="ghost"
          size="icon"
          aria-label={t('CONFIGURATION.ALARM_V2.ACTIONS')}
          className="h-8 w-8"
          onClick={(event) => {
            event.stopPropagation();
          }}
        >
          <RxDotsVertical />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        {actions.map((action, index) => (
          <React.Fragment key={index}>
            {action.separatorBefore && <DropdownMenuSeparator />}
            <DropdownMenuItem
              className={
                action.destructive
                  ? 'text-destructive focus:text-destructive hover:cursor-pointer'
                  : 'hover:cursor-pointer'
              }
              disabled={action.disabled}
              onClick={(event) => {
                event.stopPropagation();
                if (action.disabled) {
                  return;
                }
                action.onClick?.(data);
              }}
            >
              {action.label}
            </DropdownMenuItem>
          </React.Fragment>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
