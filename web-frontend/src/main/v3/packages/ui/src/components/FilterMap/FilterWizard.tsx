import React, { HTMLAttributes } from 'react';
import {
  FaQuestionCircle,
  FaRegDotCircle,
  FaChevronDown,
  FaChevronUp,
  FaExternalLinkAlt,
} from 'react-icons/fa';
import {
  AgentSelector,
  Button,
  Input,
  Label,
  Popper,
  Popover,
  PopoverContent,
  PopoverTrigger,
  RadioGroup,
  RadioGroupItem,
  Separator,
  Slider,
  cn,
} from '../..';
import { PiDotOutlineLight } from 'react-icons/pi';
import { GoDot } from 'react-icons/go';
import { FilterStatus } from './FilterStatus';
import { FilteredMap } from '@pinpoint-fe/constants';
import { addCommas, getApplicationTypeAndName } from '@pinpoint-fe/ui/utils';
import { Edge, Node } from '@pinpoint-fe/server-map';

export interface FilterWizardProps {
  className?: string;
  hideStatus?: boolean;
  openConfigures?: boolean;
  /** applied appliedFilters */
  appliedFilters?: FilteredMap.FilterState[];
  /** temp filter when click "Filter Wizard menu" */
  tempFilter?: FilteredMap.FilterState;
  onClickApply?: (filterState: FilteredMap.FilterState[]) => void;
  onClickReset?: () => void;
  onClickShowConfig?: () => void;
}

export enum TRANSACTION_RESULT {
  ALL = 'all',
  SUCESS_ONLY = 'successOnly',
  FAIL_ONLY = 'failOnly',
}

const MIN_RESPONSE_TIME = 0;
const MAX_RESPONSE_TIME = 30000;

export const getDefaultFilters = (
  data: Node | Edge,
  {
    fromAgents,
    toAgents,
    agents,
    hint,
  }: Pick<FilteredMap.FilterState, 'fromAgents' | 'toAgents' | 'agents' | 'hint'> = {},
) => {
  if ('source' in data) {
    const edgeData = data as Edge;
    // edge
    const from = getApplicationTypeAndName(edgeData.source);
    const to = getApplicationTypeAndName(edgeData.target);
    return {
      fromApplication: from?.applicationName,
      fromServiceType: from?.serviceType,
      toApplication: to?.applicationName,
      toServiceType: to?.serviceType,
      transactionResult: null,
      applicationName: '',
      serviceType: '',
      agentName: '',
      responseFrom: MIN_RESPONSE_TIME,
      responseTo: 'max',
      url: '',
      fromAgentName: '',
      toAgentName: '',
      /** for rendering agent list */
      fromAgents,
      toAgents,
      hint,
    };
  } else if ('type' in data) {
    // node
    const nodeData = data as Node;
    const app = getApplicationTypeAndName(nodeData.id);
    return {
      fromApplication: '',
      fromServiceType: '',
      toApplication: '',
      toServiceType: '',
      transactionResult: null,
      applicationName: app?.applicationName,
      serviceType: app?.serviceType,
      agentName: '',
      responseFrom: MIN_RESPONSE_TIME,
      responseTo: 'max',
      url: '',
      fromAgentName: '',
      toAgentName: '',
      /** for rendering agent list */
      agents,
      hint,
    };
  }
};

export const FilterWizard = ({
  className,
  appliedFilters = [],
  hideStatus,
  tempFilter,
  openConfigures,
  onClickApply,
  onClickReset,
  onClickShowConfig,
}: FilterWizardProps) => {
  const [selectedIndex, setSelectedIndex] = React.useState(appliedFilters.length - 1);
  const [currentFilterState, setCurrentFilterState] = React.useState(appliedFilters[selectedIndex]);
  const defaultResponseTime: [number, number] = [
    currentFilterState?.responseFrom || MIN_RESPONSE_TIME,
    (currentFilterState?.responseTo === 'max'
      ? MAX_RESPONSE_TIME
      : currentFilterState?.responseTo || MAX_RESPONSE_TIME) as number,
  ];
  const defaultTransactionResult =
    currentFilterState?.transactionResult === null ||
    currentFilterState?.transactionResult === undefined
      ? TRANSACTION_RESULT.ALL
      : currentFilterState?.transactionResult
        ? TRANSACTION_RESULT.FAIL_ONLY
        : TRANSACTION_RESULT.SUCESS_ONLY;
  const [responseTime, setResponseTime] = React.useState<[number, number]>(defaultResponseTime);
  const [transactionResult, setTransactionResult] = React.useState(defaultTransactionResult);

  React.useEffect(() => {
    if (tempFilter) {
      setCurrentFilterState(tempFilter as FilteredMap.FilterState);
    }
  }, [tempFilter]);

  React.useEffect(() => {
    if (appliedFilters?.length > 0) {
      setCurrentFilterState(appliedFilters[appliedFilters.length - 1]);
    }
  }, [appliedFilters]);

  const handleChangeTabIndex = (index: number) => {
    setSelectedIndex(index);
    setCurrentFilterState(appliedFilters[index]);
  };

  const handleChangeAgentName =
    (fieldName: 'agentName' | 'fromAgentName' | 'toAgentName') => (agentName: string) => {
      setCurrentFilterState((prev) => ({
        ...prev,
        [fieldName]: agentName,
      }));
    };

  const handleChangeUrlInput = (value: string) => {
    const url = value && (value.startsWith('/') ? value : `/${value}`);
    setCurrentFilterState((prev) => ({
      ...prev,
      url: btoa(url),
    }));
  };

  const handleClickReset = () => {
    setResponseTime(defaultResponseTime);
    setTransactionResult(defaultTransactionResult);
    setCurrentFilterState(appliedFilters[selectedIndex]);
    onClickReset?.();
  };

  const handleClickApply = () => {
    // filter override
    const newFilter = {
      ...currentFilterState,
      responseFrom: responseTime[0],
      responseTo: responseTime[1] === MAX_RESPONSE_TIME ? 'max' : responseTime[1],
      transactionResult:
        transactionResult === TRANSACTION_RESULT.ALL
          ? null
          : transactionResult === TRANSACTION_RESULT.SUCESS_ONLY
            ? false
            : true,
    };
    const newFilters = [...appliedFilters].filter((filter) => {
      if (newFilter.applicationName) {
        return (
          filter.applicationName !== newFilter.applicationName &&
          filter.serviceType !== newFilter.serviceType
        );
      } else {
        return (
          filter.fromApplication !== newFilter.fromApplication &&
          filter.fromServiceType !== newFilter.fromServiceType &&
          filter.toApplication !== newFilter.toApplication &&
          filter.toServiceType !== newFilter.toServiceType
        );
      }
    });

    newFilters.push(newFilter);
    onClickApply?.(newFilters);
  };

  return (
    <div className={cn('w-[340px] max-w-[340px] min-w-[340px]', className)}>
      {/* path: Node to Node */}
      {/* agent: Server to Server */}
      {!hideStatus && (
        <FilterStatus
          tabIndex={selectedIndex}
          onChangeTabIndex={handleChangeTabIndex}
          filterStates={appliedFilters}
          toggler={
            <Button
              variant="ghost"
              className="gap-2 text-sm w-44 text-muted-foreground"
              onClick={onClickShowConfig}
            >
              {openConfigures ? 'hide ' : 'show configures '}
              {openConfigures ? <FaChevronUp /> : <FaChevronDown />}
            </Button>
          }
        />
      )}
      {openConfigures && (
        <div>
          {!hideStatus && <Separator />}
          <FilterBox header="Path">
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: '24px calc(100% - 24px)',
              }}
            >
              <div className="flex py-2.5 pt-7 flex-col justify-evenly">
                <GoDot />
                <div className="flex flex-col text-muted-foreground">
                  <PiDotOutlineLight />
                  <PiDotOutlineLight />
                  <PiDotOutlineLight />
                </div>
                <FaRegDotCircle fill="var(--primary)" />
              </div>
              <div className="flex flex-col gap-3">
                {currentFilterState?.applicationName ? (
                  <>
                    <AgentSelector />
                    <AgentSelector
                      application={{
                        applicationName: currentFilterState?.applicationName,
                        serviceType: currentFilterState?.serviceType,
                      }}
                      agent={currentFilterState?.agentName}
                      agents={currentFilterState?.agents}
                      onChangeAgent={handleChangeAgentName('agentName')}
                    />
                  </>
                ) : (
                  <>
                    <AgentSelector
                      application={{
                        applicationName: currentFilterState?.fromApplication,
                        serviceType: currentFilterState?.fromServiceType,
                      }}
                      agent={currentFilterState?.fromAgentName}
                      agents={currentFilterState?.fromAgents}
                      onChangeAgent={handleChangeAgentName('fromAgentName')}
                    />
                    <AgentSelector
                      application={{
                        applicationName: currentFilterState?.toApplication,
                        serviceType: currentFilterState?.toServiceType,
                      }}
                      agent={currentFilterState?.toAgentName}
                      agents={currentFilterState?.toAgents}
                      onChangeAgent={handleChangeAgentName('toAgentName')}
                    />
                  </>
                )}
              </div>
            </div>
          </FilterBox>
          {/* URL Pattern */}
          <FilterBox
            header={
              <>
                URL Pattern
                <Popover>
                  <PopoverTrigger className="flex">
                    <FaQuestionCircle />
                  </PopoverTrigger>
                  <PopoverContent side="right" className="w-96">
                    <div className="text-xs text-muted-foreground">
                      Description of the URL pattern.
                    </div>
                    <div className="grid grid-cols-[6rem_auto] gap-2 items-center text-xs p-4 [&>div:nth-of-type(odd)]:text-center">
                      <div>*</div>
                      <div>Matchers zero or more characters</div>
                      <div>?</div>
                      <div>Matchers exactly one characters</div>
                      <div>**</div>
                      <div>Matchers zero or more directories</div>
                    </div>
                    <Separator />
                    <div className="grid grid-cols-[6rem_auto] gap-2 items-center text-xs p-4">
                      <div className="text-center">Example</div>
                      <div className="space-y-2">
                        <div>/pinpoint/**/*.html</div>
                        <div>/pinpoint/??.html</div>
                        <div>/pinpoint/**/??.html</div>
                      </div>
                    </div>
                  </PopoverContent>
                </Popover>
              </>
            }
          >
            <Input
              className="text-xs"
              type="text"
              placeholder="Request URL Pattern(/pinpoint/**/??.html)"
              value={atob(currentFilterState?.url || '')}
              onChange={(e) => handleChangeUrlInput(e.target.value)}
            />
          </FilterBox>
          {/* ResponseTime */}
          <FilterBox header="Response Time" style={{ paddingRight: 36 }}>
            <div className="relative px-5 mt-12">
              <div className="absolute -bottom-5 left-1/2 -translate-x-2/4">
                Range {addCommas(responseTime[1] - responseTime[0])}ms
              </div>
              <Slider
                range
                min={MIN_RESPONSE_TIME}
                max={MAX_RESPONSE_TIME}
                pushable
                value={responseTime}
                marks={{ 0: '0ms', 30000: '30,000ms' }}
                onChange={(params) => setResponseTime(params as [number, number])}
                handleRender={(origin, data) => {
                  return (
                    <Popper
                      positionUpdatable
                      shouldAlwaysShow
                      content={
                        <div className="p-2 border rounded shadow-md">
                          {addCommas(data.value)}ms
                        </div>
                      }
                      renderArrow={() => <div className="pp-arrow-sm -bottom-[0.18rem]" />}
                      placement={'top'}
                    >
                      {origin}
                    </Popper>
                  );
                }}
              />
            </div>
          </FilterBox>
          {/* TransactionResult */}
          <FilterBox header="Transaction Result">
            <RadioGroup
              value={transactionResult}
              onValueChange={(value) => {
                setTransactionResult(value as TRANSACTION_RESULT);
              }}
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem id={TRANSACTION_RESULT.ALL} value={TRANSACTION_RESULT.ALL} />
                <Label className="text-xs cursor-pointer" htmlFor={TRANSACTION_RESULT.ALL}>
                  All
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem
                  id={TRANSACTION_RESULT.SUCESS_ONLY}
                  value={TRANSACTION_RESULT.SUCESS_ONLY}
                />
                <Label className="text-xs cursor-pointer" htmlFor={TRANSACTION_RESULT.SUCESS_ONLY}>
                  Success Only
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem
                  id={TRANSACTION_RESULT.FAIL_ONLY}
                  value={TRANSACTION_RESULT.FAIL_ONLY}
                />
                <Label className="text-xs cursor-pointer" htmlFor={TRANSACTION_RESULT.FAIL_ONLY}>
                  Failed Only
                </Label>
              </div>
            </RadioGroup>
          </FilterBox>
          <div className="flex flex-row-reverse gap-2 px-4 py-5 mt-3">
            <Button className="text-sm" onClick={handleClickApply}>
              Apply Filters <FaExternalLinkAlt className="ml-1.5" />
            </Button>
            <Button variant="outline" className="text-sm" onClick={handleClickReset}>
              Reset
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

interface FilterBoxProps extends HTMLAttributes<HTMLDivElement> {
  header?: React.ReactNode;
  children?: React.ReactNode;
}

const FilterBox = ({ header, children, ...props }: FilterBoxProps) => {
  return (
    <div className="flex flex-col gap-3 p-5 pr-6 text-xs rounded" {...props}>
      <div className="flex items-center gap-2 text-sm font-semibold">{header}</div>
      <div className="pl-1">{children}</div>
    </div>
  );
};
