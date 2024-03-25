import { HiOutlineArrowRight } from 'react-icons/hi';
import { getApplicationTypeAndName, getServerImagePath } from '@pinpoint-fe/utils';
import { Edge } from '@pinpoint-fe/server-map';

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
    <div className="flex items-center h-12 gap-1 px-1 text-lg font-semibold shrink-0 border-b-1">
      {currentTarget &&
        (currentTarget?.type === 'node' ? (
          <>
            <img src={currentTarget?.imgPath} width={52} />
            <div className="truncate">{currentTarget?.applicationName}</div>
          </>
        ) : (
          (() => {
            const sourceApp = getApplicationTypeAndName(currentTarget?.source)!;
            const targetApp = currentTarget.edges
              ? {
                  applicationName: `total: ${currentTarget.edges.length}`,
                  serviceType: getApplicationTypeAndName(currentTarget.edges[0].target)!
                    .serviceType,
                }
              : getApplicationTypeAndName(currentTarget?.target)!;

            return (
              <div className="flex items-center justify-between w-full">
                <div className="flex items-center max-w-[45%] w-[40%]">
                  <img src={getServerImagePath(sourceApp)} width={52} />
                  <div className="truncate">{sourceApp?.applicationName}</div>
                </div>
                <HiOutlineArrowRight />
                <div className="flex items-center max-w-[45%] w-[40%]">
                  <img src={getServerImagePath(targetApp)} width={52} />
                  <div className="truncate">{targetApp?.applicationName}</div>
                </div>
              </div>
            );
          })()
        ))}
    </div>
  );
};
