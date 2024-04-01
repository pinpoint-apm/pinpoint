import { ModifiedActiveTotalThreadStatus } from './useActiveThread';
import { cn } from '../../lib';

export interface RequestCounterProps {
  className?: string;
  thread?: ModifiedActiveTotalThreadStatus;
}

export const RequestCounter = ({ className, thread }: RequestCounterProps) => {
  return (
    <div className={cn('text-xs grid grid-cols-2 items-center justify-items-end', className)}>
      <div className="text-right min-w-8">Total</div>
      <div className=" bg-slate-400/75 text-white flex h-3.5 w-5 items-center justify-center rounded">
        {thread?.status.reduce((acc, curr) => {
          return acc + curr;
        }, 0)}
      </div>
      <div className="text-right min-w-8">Slow</div>
      <div className="bg-status-fail/75 text-white flex h-3.5 w-5 items-center justify-center rounded">
        {thread?.status[3] || 0}
      </div>
      <div className="text-right min-w-8">5s</div>
      <div className="bg-status-warn/75 text-white flex h-3.5 w-5 items-center justify-center rounded">
        {thread?.status[2] || 0}
      </div>
      <div className="text-right min-w-8">3s</div>
      <div className="bg-status-good/75 text-white flex h-3.5 w-5 items-center justify-center rounded">
        {thread?.status[1] || 0}
      </div>
      <div className="text-right min-w-8">1s</div>
      <div className="bg-status-success/75 text-white flex h-3.5 w-5 items-center justify-center rounded">
        {thread?.status[0] || 0}
      </div>
    </div>
  );
};
