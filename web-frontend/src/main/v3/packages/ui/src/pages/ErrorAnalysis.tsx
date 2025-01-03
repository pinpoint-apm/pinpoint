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
} from '@pinpoint-fe/ui/utils';
import { useErrorAnalysisSearchParameters } from '@pinpoint-fe/ui/hooks';
import { useTranslation } from 'react-i18next';
import { ErrorAnalysisErrorList, BASE_PATH } from '@pinpoint-fe/ui/constants';
import { format } from 'date-fns';
import { IoMdClose } from 'react-icons/io';
import { PiBugBeetleDuotone } from 'react-icons/pi';
import { LuExternalLink } from 'react-icons/lu';
import { FaChevronRight } from 'react-icons/fa6';
import { ServerIcon } from '../components/Application/ServerIcon';

export interface ErrorAnalysisPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const ErrorAnalysisPage = ({
  ApplicationList = ApplicationCombinedList,
}: ErrorAnalysisPageProps) => {
  const navigate = useNavigate();

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
              maxDateRangeDays={7}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: 7,
              })}
              timeUnits={['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d', '7d']}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <LayoutWithContentSidebar contentWrapperClassName="h-fit">
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
                  <SheetDescription className="space-y-2 font-semibold">
                    <div className="flex items-center gap-1.5">
                      <ServerIcon className="w-4" application={application} />
                      <h3 className="text-base font-medium truncate text-foreground">
                        {application.applicationName}
                      </h3>
                      <div className="pr-2 ml-auto text-xs font-medium truncate">
                        {format(errorInfo.timestamp, 'MM.dd HH:mm:ss SSS')}
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
