import React from 'react';
import {
  ApplicationCombinedList,
  ApplicationCombinedListProps,
  DatetimePicker,
  DatetimePickerChangeHandler,
  LayoutWithContentSidebar,
  MainHeader,
  UrlSidebar,
  UrlStatChart,
  UrlSummary,
} from '../components';
import { useNavigate } from 'react-router-dom';
import { convertParamsToQueryString, getUrlStatPath } from '@pinpoint-fe/ui/src/utils';
import { useUrlStatSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import { PiChartBarDuotone } from 'react-icons/pi';
// import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@pinpoint-fe/ui/src/components';
import { UrlStatSummary } from '../constants';

const TAB_LIST = [
  { id: 'total', display: 'Total Count' },
  { id: 'failure', display: 'Failure Count' },
  { id: 'apdex', display: 'Apdex' },
  { id: 'latency', display: 'Latency' },
];

type TYPE = UrlStatSummary.Parameters['type'];

export interface UrlStatisticPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const UrlStatisticPage = ({
  ApplicationList = ApplicationCombinedList,
}: UrlStatisticPageProps) => {
  const navigate = useNavigate();
  const { searchParameters, application, agentId } = useUrlStatSearchParameters();
  const { t } = useTranslation();
  const [type, setType] = React.useState<TYPE>('total');

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate }) => {
      navigate(
        `${getUrlStatPath(application!)}?${convertParamsToQueryString({
          ...formattedDate,
          ...{ agentId },
        })}`,
      );
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, agentId],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiChartBarDuotone /> URL Statistic
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => navigate(getUrlStatPath(application))}
        />
        <div className="ml-auto">
          {application && (
            <DatetimePicker
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              maxDateRangeDays={28}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: 28,
              })}
              timeUnits={['5m', '20m', '1h', '12h', '1d', '7d', '14d', '28d']}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <LayoutWithContentSidebar>
          <UrlSidebar />
          <>
            <Tabs
              defaultValue="total"
              className="p-3 bg-white border rounded"
              onValueChange={(value) => setType(value as TYPE)}
            >
              <TabsList>
                {TAB_LIST.map((tab) => (
                  <TabsTrigger key={tab.id} value={tab.id}>
                    {tab.display}
                  </TabsTrigger>
                ))}
              </TabsList>
              {TAB_LIST.map((tab) => (
                <TabsContent key={tab.id} value={tab.id} className="h-72">
                  <UrlStatChart
                    type={tab.id}
                    emptyMessage={t('COMMON.NO_DATA')}
                    guideMessage={t('URL_STAT.SELECT_URL_INFO')}
                  />
                </TabsContent>
              ))}
            </Tabs>
            <UrlSummary type={type} />
          </>
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
