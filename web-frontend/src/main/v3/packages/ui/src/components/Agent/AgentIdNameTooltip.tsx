import { Tooltip, TooltipContent, TooltipTrigger, TooltipPortal } from '../..';

export const AgentIdNameTooltip = ({
  children,
  agentId,
  agentName,
  usePortal = false,
  side,
}: {
  children: React.ReactNode;
  agentId: string;
  agentName: string;
  usePortal?: boolean;
  /**
   * 툴팁이 붙을 방향. 넘기지 않으면 기본값인 위쪽이다.
   *
   * 세로로 늘어선 목록 위에서는 위쪽 툴팁이 바로 윗줄을 가린다. 목록 바깥에 여백이 있는
   * 화면이라면 옆으로 빼는 편이 낫다.
   */
  side?: React.ComponentProps<typeof TooltipContent>['side'];
}) => {
  const content = (
    <TooltipContent side={side}>
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
