import { Tooltip, TooltipContent, TooltipTrigger, TooltipPortal } from '../..';

export const AgentIdNameTooltip = ({
  children,
  agentId,
  agentName,
  usePortal = false,
}: {
  children: React.ReactNode;
  agentId: string;
  agentName: string;
  usePortal?: boolean;
}) => {
  const content = (
    <TooltipContent>
      <div>
        <span className="text-gray-500">Agent ID:</span> {agentId}
      </div>
      <div>
        <span className="text-gray-500">Agent Name:</span> {agentName}
      </div>
    </TooltipContent>
  );

  return (
    <Tooltip>
      <TooltipTrigger asChild>{children}</TooltipTrigger>
      {usePortal ? <TooltipPortal>{content}</TooltipPortal> : content}
    </Tooltip>
  );
};
