import React from 'react';
import { Tab } from '@headlessui/react';
import { FilteredMapType as FilteredMap } from '@pinpoint-fe/ui/constants';
import { getServerImagePath } from '@pinpoint-fe/ui/utils';
import { HiOutlineArrowRight } from 'react-icons/hi';
import { Badge, cn } from '../..';

export interface FilterStatusProps {
  toggler?: React.ReactNode;
  tabIndex?: number;
  onChangeTabIndex?: (index: number) => void;
  filterStates?: FilteredMap.FilterState[];
}

export const FilterStatus = ({
  toggler,
  filterStates,
  tabIndex,
  onChangeTabIndex,
}: FilterStatusProps) => {
  const getTransactionResultText = (
    transactionResult: FilteredMap.FilterState['transactionResult'],
  ) => {
    if (transactionResult === null) {
      return 'All';
    } else if (transactionResult) {
      return 'Failed Only';
    } else {
      return 'Success Only';
    }
  };
  return (
    <div>
      <Tab.Group selectedIndex={tabIndex} onChange={onChangeTabIndex}>
        <Tab.List className="px-2 bg-gray-100 rounded-t">
          {filterStates?.map((state, i) => (
            <Tab
              key={i}
              className={cn('py-2 pt-3 px-1 rounded-t', { 'pt-2 bg-background': tabIndex === i })}
            >
              <Servers filterState={state} minimize={true} />
            </Tab>
          ))}
        </Tab.List>
        <Tab.Panels>
          {filterStates?.map((state, i) => (
            <Tab.Panel key={i} className="px-4">
              <div className="flex items-center h-12 px-1 mb-1 font-semibold">Applied Filters</div>
              <div className="px-2">
                <div className="flex items-center gap-2 text-xs text-muted-foreground h-9">
                  Application:
                  <Badge variant="secondary">
                    <Servers filterState={state} />
                  </Badge>
                </div>
                <div className="flex items-center gap-2 text-xs text-muted-foreground h-9">
                  Agent:
                  <Badge variant="secondary" className="gap-2">
                    {state.applicationName ? (
                      state.agentName || 'All'
                    ) : (
                      <>
                        {state.fromAgentName || 'All'}
                        <HiOutlineArrowRight />
                        {state.toAgentName || 'All'}
                      </>
                    )}
                  </Badge>
                </div>
                {state.url && (
                  <div className="flex items-center gap-2 text-xs text-muted-foreground h-9">
                    URL Pattern:
                    <Badge variant="secondary">{atob(state.url || '')}</Badge>
                  </div>
                )}
                <div className="flex items-center gap-2 text-xs text-muted-foreground h-9">
                  Response Time:
                  <Badge variant="secondary">
                    {state.responseFrom} ~ {state.responseTo}
                  </Badge>
                </div>
                <div className="flex items-center gap-2 text-xs text-muted-foreground h-9">
                  Transaction Result:
                  <Badge variant="secondary">
                    {getTransactionResultText(state.transactionResult)}
                  </Badge>
                </div>
              </div>
            </Tab.Panel>
          ))}
        </Tab.Panels>
      </Tab.Group>
      <div className="flex items-center justify-center px-2 py-1 mt-5 mb-4 rounded">{toggler}</div>
    </div>
  );
};

const Servers = (props: {
  filterState?: FilteredMap.FilterState;
  minimize?: boolean;
  className?: string;
}) => {
  const { filterState, minimize = false, className } = props;
  const iconWidth = 28;
  return (
    <div className={cn('flex gap-1 items-center', className)}>
      {filterState?.applicationName ? (
        <div className="flex items-center font-semibold">
          {minimize && (
            <img
              src={getServerImagePath({
                serviceType: filterState.serviceType,
                applicationName: filterState.applicationName,
              })}
              width={iconWidth}
              alt="server-from-image"
            />
          )}
          {!minimize && <div className="truncate">{filterState.applicationName}</div>}
        </div>
      ) : (
        <>
          <div className="flex items-center font-semibold max-w-[92px]">
            {minimize && (
              <img
                src={getServerImagePath({
                  serviceType: filterState?.fromServiceType,
                  applicationName: filterState?.fromApplication,
                })}
                width={iconWidth}
                alt="server-from-image"
              />
            )}
            {!minimize && <div className="truncate">{filterState?.fromApplication}</div>}
          </div>
          {!minimize && <HiOutlineArrowRight />}
          <div className="flex items-center font-semibold max-w-[92px]">
            {minimize && (
              <img
                src={getServerImagePath({
                  serviceType: filterState?.toServiceType,
                  applicationName: filterState?.toApplication,
                })}
                width={iconWidth}
                alt="server-to-image"
              />
            )}
            {!minimize && <div className="truncate">{filterState?.toApplication}</div>}
          </div>
        </>
      )}
    </div>
  );
};
