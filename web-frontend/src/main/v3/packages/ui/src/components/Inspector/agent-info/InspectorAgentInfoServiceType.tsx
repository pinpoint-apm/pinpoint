import React from 'react';
import { InspectorAgentInfoType as InspectorAgentInfo } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../../lib';
import { InfoDefinition } from './InspectorAgentInfoFetcher';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '../../ui';

export interface InspectorAgentInfoServiceTypeProps {
  data: InspectorAgentInfo.Response['serverMetaData'];
  className?: string;
}

export const InspectorAgentInfoServiceType = ({
  data,
  className,
}: InspectorAgentInfoServiceTypeProps) => {
  const { serverInfo, serviceInfos, vmArgs } = data;
  const serviceTypeDefinitionList: InfoDefinition[] = [
    {
      key: 'serverInfo',
      label: 'Server Info',
      value: serverInfo,
      show: Boolean(serverInfo),
    },
    {
      key: 'vmArgs',
      label: 'JVM Arguments',
      show: vmArgs.length !== 0,
      renderer: () => {
        return (
          <ul className="h-[150px] overflow-y-auto list-none">
            {vmArgs.map((vmArg: string, i: number) => (
              <li key={i} className="w-full">
                {vmArg}
              </li>
            ))}
          </ul>
        );
      },
    },
    {
      key: 'serviceInfos',
      label: 'Services',
      show: serviceInfos.length !== 0,
      renderer: () => {
        return (
          <Accordion type="single" collapsible>
            <ul className="list-none">
              {serviceInfos.map(({ serviceName, serviceLibs }) => (
                <AccordionItem value={serviceName} key={serviceName}>
                  <AccordionTrigger className="py-1 text-xs">
                    <li>{serviceName}</li>
                  </AccordionTrigger>
                  <AccordionContent className="text-xs">
                    <ul className="h-[150px] overflow-y-auto list-none">
                      {serviceLibs.map((lib: string, i: number) => (
                        <li key={i}>{lib}</li>
                      ))}
                    </ul>
                  </AccordionContent>
                </AccordionItem>
              ))}
            </ul>
          </Accordion>
        );
      },
    },
  ];
  return (
    <div className={cn('w-full', className)}>
      <dl className="">
        {serviceTypeDefinitionList.map(
          ({ key, label, value, show, renderer }) =>
            show && (
              <React.Fragment key={key}>
                <dt className="py-1 text-xs font-semibold">{label}</dt>
                <dd className="py-1 text-xs">{renderer ? renderer({ key, value }) : value}</dd>
              </React.Fragment>
            ),
        )}
      </dl>
    </div>
  );
};
