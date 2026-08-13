import { HiOutlineArrowRight } from 'react-icons/hi';
import { parseNodeApplication } from '@pinpoint-fe/ui/src/utils';
import { Edge } from '@pinpoint-fe/server-map';
import { ServerIcon } from '../Application/ServerIcon';

export interface ChartsBoardHeaderProps {
  currentTarget: {
    imgPath?: string;
    applicationName?: string;
    type?: string;
    source?: string;
    target?: string;
    edges?: Edge[];
  } | null;
}

export const ChartsBoardHeader = ({ currentTarget }: ChartsBoardHeaderProps) => {
  return (
    <div className="flex items-center h-12 gap-2 px-2 text-lg font-semibold shrink-0 border-b-1">
      {currentTarget &&
        (currentTarget?.type === 'node' ? (
          <>
            <ServerIcon application={currentTarget} className="" />
            <div className="truncate">{currentTarget?.applicationName}</div>
          </>
        ) : (
          (() => {
            // 링크의 source/target은 map 노드의 id다. servicemap 응답에서는 3-part
            // (serviceName^applicationName^serviceType)이므로 URL 세그먼트를 파싱하는
            // `getApplicationTypeAndName`으로 읽으면 이름 앞에 serviceName이 붙어 보인다.
            const sourceApp = parseNodeApplication(currentTarget?.source);
            const targetApp = currentTarget.edges
              ? {
                  applicationName: `total: ${currentTarget.edges.length}`,
                  serviceType: parseNodeApplication(currentTarget.edges[0].target)?.serviceType,
                }
              : parseNodeApplication(currentTarget?.target);

            return (
              <div className="flex items-center justify-between w-full">
                <div className="flex items-center flex-1 gap-2">
                  <ServerIcon application={sourceApp ?? {}} className="" />
                  <div className="truncate">{sourceApp?.applicationName}</div>
                </div>
                <div className="w-5 mx-3">
                  <HiOutlineArrowRight />
                </div>
                <div className="flex items-center flex-1 gap-2">
                  <ServerIcon application={sourceApp ?? {}} className="" />
                  <div className="truncate">{targetApp?.applicationName}</div>
                </div>
              </div>
            );
          })()
        ))}
    </div>
  );
};
