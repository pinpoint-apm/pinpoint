import React from 'react';
import { RxChevronDown, RxChevronRight } from 'react-icons/rx';
import { format } from 'date-fns';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '../../ui';
import { useGetInspectorAgentInfoData } from '@pinpoint-fe/ui/hooks';
import { InspectorAgentInfoServiceType } from './InspectorAgentInfoServiceType';
import { LuChevronsUpDown } from 'react-icons/lu';
import { insertIf } from '@pinpoint-fe/utils';

export type InfoDefinition = {
  key: string;
  label: string;
  value?: string | number;
  show?: boolean;
  renderer?: (props: Pick<InfoDefinition, 'key' | 'value'>) => React.ReactNode | null;
};

export interface InspectorAgentInfoFetcherProps {}

const AGENT_INFO_TIMESTAMP_FORMAT = 'yyyy.MM.dd HH:mm:ss XXX';
export const InspectorAgentInfoFetcher = () => {
  const { data } = useGetInspectorAgentInfoData();
  const agentInfoDefinitionList: InfoDefinition[] = data
    ? [
        {
          key: 'applicationName',
          label: 'Application Name',
          value: data.applicationName,
        },
        {
          key: 'agentVersion',
          label: 'Agent Version',
          value: data.agentVersion,
        },
        {
          key: 'pid',
          label: 'PID',
          value: data.pid,
        },
        {
          key: 'hostName',
          label: 'Hostname',
          value: data.hostName,
        },
        ...insertIf(!!data.jvmInfo, () => {
          return [
            {
              key: 'jvmInfo',
              label: 'JVM (GC Type)',
              value: `${data.jvmInfo.jvmVersion} (${data.jvmInfo.gcTypeName})`,
            },
          ];
        }),
        {
          key: 'ip',
          label: 'IP',
          value: data.ip,
        },
        {
          key: 'startTime',
          label: 'Start Time',
          value: format(data.startTimestamp, AGENT_INFO_TIMESTAMP_FORMAT),
        },
        {
          key: 'endStatus',
          label: 'End Status',
          value: `${data.status.state.desc} (last checked: ${format(
            data.status.eventTimestamp,
            AGENT_INFO_TIMESTAMP_FORMAT,
          )})`,
        },
        {
          key: 'serviceType',
          label: 'Service Type',
          value: `${data?.serviceType}`,
          renderer: ({ value }) => {
            return data.serverMetaData ? (
              <Popover>
                <PopoverTrigger className="flex items-center gap-1 pb-1 border-b-1">
                  <span className="flex-1">
                    {value}
                    {data.serverMetaData.serverInfo && ` (${data.serverMetaData.serverInfo})`}
                  </span>
                  <LuChevronsUpDown className="ml-auto" />
                </PopoverTrigger>
                <PopoverContent className="w-[500px] xl:w-[650px]">
                  <InspectorAgentInfoServiceType data={data.serverMetaData} />
                </PopoverContent>
              </Popover>
            ) : (
              value
            );
          },
        },
      ]
    : [];
  const [isOpen, setIsOpen] = React.useState(true);

  return (
    <Collapsible open={isOpen} onOpenChange={setIsOpen} className="bg-white border rounded">
      <div className="flex px-4 py-2">
        <CollapsibleTrigger className="mr-auto cursor-pointer" asChild>
          <div className="flex items-center gap-1">
            {isOpen ? <RxChevronDown /> : <RxChevronRight />}
            <span className="font-semibold">{data?.agentId}</span>{' '}
          </div>
        </CollapsibleTrigger>
      </div>
      <CollapsibleContent className="px-4">
        <dl className="grid gap-1 pb-2 md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3">
          {agentInfoDefinitionList.map(({ key, label, value, renderer }) => (
            <React.Fragment key={key}>
              <div className="flex items-center gap-1">
                <dt className="py-1.5 font-semibold text-xs w-2/6">{label}</dt>
                <dd className="py-1.5 text-xs flex-1">
                  {renderer ? renderer({ key, value }) : value}
                </dd>
              </div>
            </React.Fragment>
          ))}
        </dl>
      </CollapsibleContent>
    </Collapsible>
  );
};
