import React from 'react';
import {
  Button,
  ErrorAnalysisSidebar,
  ErrorAnalysisChart,
  ErrorAnalysisErrorDetail,
  MainHeader,
  Separator,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  ErrorAnalysisTable,
  ErrorAnalysisGroupedTable,
  DatetimePickerChangeHandler,
  DatetimePicker,
  LayoutWithContentSidebar,
  ApplicationCombinedList,
  ApplicationCombinedListProps,
} from '../components';
import { useNavigate } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getErrorAnalysisPath,
  getTransactionDetailPath,
  getTransactionDetailQueryString,
} from '@pinpoint-fe/ui/src/utils';
import { useErrorAnalysisSearchParameters, useTimezone } from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import {
  ErrorAnalysisErrorList,
  BASE_PATH,
  Configuration,
  APP_SETTING_KEYS,
} from '@pinpoint-fe/ui/src/constants';
import { formatInTimeZone } from 'date-fns-tz';
import { IoMdClose } from 'react-icons/io';
import { PiBugBeetleDuotone } from 'react-icons/pi';
import { LuExternalLink } from 'react-icons/lu';
import { FaChevronRight } from 'react-icons/fa6';
import { ServerIcon } from '../components/Application/ServerIcon';

export interface ErrorAnalysisPageProps {
  configuration?: Configuration;
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const ErrorAnalysisPage = ({
  configuration,
  ApplicationList = ApplicationCombinedList,
}: ErrorAnalysisPageProps) => {
  const periodMax = configuration?.['periodMax.exceptionTrace'];
  const periodInterval = configuration?.['periodInterval.exceptionTrace'];
  const navigate = useNavigate();
  const [timezone] = useTimezone();
  const {
    searchParameters,
    application,
    agentId,
    groupBy,
    parsedTransactionInfo,
    openErrorDetail,
  } = useErrorAnalysisSearchParameters();
  const [open, setOpen] = React.useState<boolean>(openErrorDetail);
  const [errorInfo, setErrorInfo] =
    React.useState<ErrorAnalysisErrorList.ErrorData>(parsedTransactionInfo);

  const { t } = useTranslation();

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate }) => {
      navigate(
        `${getErrorAnalysisPath(application!)}?${convertParamsToQueryString({
          ...formattedDate,
          ...{ agentId, groupBy },
        })}`,
      );
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, agentId, groupBy],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiBugBeetleDuotone /> Error Analysis
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => navigate(getErrorAnalysisPath(application))}
        />
        <div className="ml-auto">
          {application && (
            <DatetimePicker
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              maxDateRangeDays={periodMax}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: periodMax,
              })}
              timeUnits={periodInterval}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <LayoutWithContentSidebar
          autoSaveId={APP_SETTING_KEYS.ERROR_ANALYSIS_RESIZABLE}
          contentWrapperClassName="h-fit"
        >
          <ErrorAnalysisSidebar />
          <>
            <div className="py-2 bg-white border rounded h-72">
              <ErrorAnalysisChart emptyMessage={t('COMMON.NO_DATA')} />
            </div>
            {groupBy ? (
              <ErrorAnalysisGroupedTable className="mt-20" />
            ) : (
              <ErrorAnalysisTable
                onClickRow={(row) => {
                  setOpen(true);
                  setErrorInfo(row.original);
                }}
              />
            )}
          </>
          {errorInfo && (
            <Sheet open={open} onOpenChange={setOpen}>
              <SheetContent
                className="flex flex-col gap-0 w-3/5 sm:max-w-full z-[5000] px-0 py-4"
                overlayClassName="bg-transparent backdrop-blur-none"
                hideClose={true}
              >
                <SheetHeader className="px-6">
                  <SheetTitle className="flex items-center gap-1">
                    <div className="w-1 h-4 rounded-sm bg-status-fail" />
                    Error Detail{' '}
                    <Button
                      variant="outline"
                      size="icon"
                      className="ml-auto border-none shadow-none color-"
                      onClick={() => setOpen(!open)}
                    >
                      <IoMdClose className="w-5 h-5" />
                    </Button>
                  </SheetTitle>
                  <SheetDescription className="space-y-2 font-semibold" asChild>
                    <div>
                      <div className="flex items-center gap-1.5">
                        <ServerIcon className="w-4" application={application} />
                        <h3 className="text-base font-medium truncate text-foreground">
                          {application.applicationName}
                        </h3>
                        <div className="pr-2 ml-auto text-xs font-medium truncate">
                          {formatInTimeZone(errorInfo.timestamp, timezone, 'MM.dd HH:mm:ss SSS')}
                        </div>
                      </div>
                      <div className="flex items-center">
                        <div className="truncate text-foreground">{errorInfo.agentId}</div>
                        <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
                        <div className="truncate text-foreground">{errorInfo.uriTemplate}</div>
                        <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
                        <div
                          className="flex items-center gap-1 truncate cursor-pointer hover:text-primary hover:underline"
                          onClick={() => {
                            window.open(
                              `${BASE_PATH}${getTransactionDetailPath(
                                application,
                              )}?${getTransactionDetailQueryString({
                                agentId: errorInfo.agentId,
                                spanId: `${errorInfo.spanId}`,
                                traceId: errorInfo.transactionId,
                                focusTimestamp: errorInfo.timestamp,
                              })}`,
                            );
                          }}
                        >
                          {errorInfo.transactionId}
                          <LuExternalLink className="ml-1" />
                        </div>
                      </div>
                    </div>
                  </SheetDescription>
                </SheetHeader>
                <Separator className="mt-[20px]" />
                <ErrorAnalysisErrorDetail errorInfo={errorInfo} />
              </SheetContent>
            </Sheet>
          )}
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
