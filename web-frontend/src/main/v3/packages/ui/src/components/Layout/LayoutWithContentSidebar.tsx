import { cn } from '../../lib';

export interface LayoutWithContentSidebarProps {
  children: React.ReactNode[];
  contentWrapperClassName?: string;
}

export const LayoutWithContentSidebar = ({
  children,
  contentWrapperClassName,
}: LayoutWithContentSidebarProps) => {
  const [sidebar, content, ...rest] = children;
  return (
    <div className="grid grid-cols-[15rem_auto] h-[calc(100%-4rem)]">
      <div className="w-60 min-w-[15rem] border-r-1 h-full overflow-auto scrollbar-hide">
        {sidebar}
      </div>
      <div className="flex flex-wrap justify-center w-full p-5 pt-4 pb-10 overflow-auto bg-primary-foreground">
        <div className={cn('flex flex-col w-full h-full max-w-8xl', contentWrapperClassName)}>
          {content}
        </div>
      </div>
      {rest}
    </div>
  );
};
