import { GetServerMap } from '@pinpoint-fe/ui/constants';
import { cn } from '../../lib';

export interface InstanceCountProps {
  className?: string;
  nodeData?: GetServerMap.NodeData;
}

export const InstanceCount = ({
  className,
  nodeData = {} as GetServerMap.NodeData,
}: InstanceCountProps) => {
  const { instanceCount, instanceErrorCount } = nodeData;

  return (
    <div className={cn('flex text-muted-foreground gap-6 text-sm ml-auto', className)}>
      <span>
        Total <span className="ml-1 text-base font-semibold text-foreground">{instanceCount}</span>
      </span>
      <span>
        Error{' '}
        <span className="ml-1 text-base font-semibold text-status-fail">{instanceErrorCount}</span>
      </span>
    </div>
  );
};
