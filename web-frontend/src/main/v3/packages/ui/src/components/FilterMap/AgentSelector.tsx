import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

import { Select, SelectTrigger, SelectValue, SelectContent, SelectGroup, SelectItem } from '../ui';
import { ServerIcon } from '../Application/ServerIcon';

export interface AgentSelectorProps {
  application?: ApplicationType;
  agent?: string;
  agents?: string[];
  onChangeAgent?: (agentName: string) => void;
}

const DEFAULT_AGENT_NAME = 'All';

export const AgentSelector = ({
  application,
  agents,
  agent = DEFAULT_AGENT_NAME,
  onChangeAgent,
}: AgentSelectorProps) => {
  const agentList = agents ? [DEFAULT_AGENT_NAME, ...agents] : [DEFAULT_AGENT_NAME];

  return (
    <div>
      <div className="flex items-center gap-1.5 mb-1">
        {application && <ServerIcon application={application} className="w-4" />}
        <div className="text-xs truncate text-muted-foreground">
          {application?.applicationName || '(single)'}
        </div>
      </div>
      <Select
        value={agent || agentList[0]}
        disabled={agentList.length <= 1}
        onValueChange={(value) => onChangeAgent?.(value || '')}
      >
        <SelectTrigger className="w-[calc(100%-3.125rem)] text-xs">
          <span className="truncate">
            <SelectValue>{agent || agentList[0]}</SelectValue>
          </span>
        </SelectTrigger>
        <SelectContent className="max-h-96">
          <SelectGroup>
            {agentList?.map((agent, i) => (
              <SelectItem className="text-xs" key={i} value={agent}>
                {agent}
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  );
};
