import { cn } from '../../lib/utils';

export interface MainHeaderProps {
  title?: React.ReactNode;
  className?: string;
  children?: React.ReactNode;
}

export const MainHeader = ({ title, children, className }: MainHeaderProps) => {
  return (
    <div
      className={cn(
        'w-full h-16 px-7 border-b-2 flex items-center sticky top-0 bg-white z-[2000]',
        className,
      )}
    >
      <div className="mr-4 text-xl font-semibold whitespace-nowrap">{title}</div>
      {children}
    </div>
  );
};
