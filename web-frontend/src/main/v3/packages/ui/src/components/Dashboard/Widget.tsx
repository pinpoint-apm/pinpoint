import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../ui';
import { RxDotsVertical } from 'react-icons/rx';
import { useTranslation } from 'react-i18next';
import { cn } from '../../lib';
import { PiTrash } from 'react-icons/pi';
import { PiNotePencil } from 'react-icons/pi';
import { PropsWithChildren } from 'react';

export interface WidgetProps extends PropsWithChildren {
  title?: string;
  onClickEdit?: () => void;
  onClickDelete?: () => void;
}

export const DRAGGABLE_HANDLE_CLASS = '__pp_widget_draggable_hanlde__';
export const DRAGGABLE_CANCEL_CLASS = '__pp_widget_draggable_cancel__';

export const Widget = ({ title, children, onClickEdit, onClickDelete }: WidgetProps) => {
  const { t } = useTranslation();
  return (
    <div className="h-full bg-white border rounded-sm">
      <div
        className={cn(
          DRAGGABLE_HANDLE_CLASS,
          '@container',
          'flex items-center justify-between h-8 gap-2 px-1 border-b cursor-move',
        )}
      >
        <h3 className="flex-1 pl-1 text-sm truncate">{title}</h3>
        <DropdownMenu>
          <DropdownMenuTrigger className="@md:hidden">
            {/* <button  */}
            <Button variant="ghost" className="px-2 py-1 rounded-sm h-7">
              <RxDotsVertical />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="text-sm">
            <DropdownMenuItem onClick={() => onClickEdit?.()}>{t('COMMON.EDIT')}</DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className="text-destructive hover:!text-destructive"
              onClick={() => onClickDelete?.()}
            >
              {t('COMMON.DELETE')}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
        <div className={cn(DRAGGABLE_CANCEL_CLASS, 'hidden @md:flex items-center')}>
          <Button variant="hover" className="p-1 h-7" onClick={() => onClickEdit?.()}>
            <PiNotePencil />
          </Button>
          <Button variant="hover" className="p-1 h-7" onClick={() => onClickDelete?.()}>
            <PiTrash />
          </Button>
        </div>
      </div>
      {children && <div className="h-[calc(100%-2rem)]">{children}</div>}
    </div>
  );
};
