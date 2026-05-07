import { IoMdClose } from 'react-icons/io';
import { Popper } from '../Popper';
import { cn } from '../../lib';

export interface ServerMapMenuProps {
  children?: React.ReactNode;
  position: Partial<{ x: number; y: number }>;
  contentType?: SERVERMAP_MENU_CONTENT_TYPE;
  onClickFunction?: (props: { functionType: typeof SERVERMAP_MENU_FUNCTION_TYPE }) => void;
}

export enum SERVERMAP_MENU_CONTENT_TYPE {
  NODE = 1,
  EDGE,
  BACKGROUND,
  HOVER_NODE,
  SERVICE_GROUP_LIST,
}

export enum SERVERMAP_MENU_FUNCTION_TYPE {
  FILTER_TRANSACTION = 1,
  FILTER_WIZARD,
  MERGE,
}

export const ServerMapMenu = ({ children, position, contentType }: ServerMapMenuProps) => {
  return (
    <>
      {contentType && (
        <Popper
          positionUpdatable
          shouldAlwaysShow={true}
          content={<div className="py-2 text-xs border rounded shadow min-w-40">{children}</div>}
          placement={'left'}
          modifiers={[
            {
              name: 'offset',
              options: {
                offset: [position.x, position.y],
              },
            },
          ]}
          hideArrow
        />
      )}
    </>
  );
};

interface ServerMapMenuContentProps {
  title: React.ReactNode;
  children: React.ReactNode;
  className?: string;
  onClose?: () => void;
}

export const ServerMapMenuContent = ({
  title,
  children,
  className,
  onClose,
}: ServerMapMenuContentProps) => {
  return (
    <div className={cn('w-52', className)}>
      <div className="flex items-center h-8 gap-1 px-3 text-sm font-semibold">
        <span className="flex-1 truncate">{title}</span>
        {onClose && (
          <button
            type="button"
            aria-label="Close"
            className="p-0.5 text-muted-foreground hover:text-foreground"
            onClick={onClose}
          >
            <IoMdClose />
          </button>
        )}
      </div>
      {children}
    </div>
  );
};

interface ServerMapMenuItemProps {
  children: React.ReactNode | React.ReactNode[];
  className?: string;
  onClick?: () => void;
}

export const ServerMapMenuItem = ({ children, onClick, className }: ServerMapMenuItemProps) => {
  return (
    <div
      className={cn(
        'flex items-center w-full h-9 gap-2 px-3 cursor-pointer hover:bg-accent',
        className,
      )}
      onClick={() => onClick?.()}
    >
      {children}
    </div>
  );
};
