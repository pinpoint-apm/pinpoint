import React from 'react';
import { HiOutlineRefresh } from 'react-icons/hi';
import { useTranslation } from 'react-i18next';
import { Configuration } from '@pinpoint-fe/ui/constants';
import { Button, Separator } from '../../../components';
import { useGetAgentsStatistics } from '@pinpoint-fe/ui/hooks';
import { CgSpinner } from 'react-icons/cg';
import { cn } from '../../../lib';
import { AgentStatisticContainer } from './AgentStatisticChartContainer';
import { AgentStatisticTable } from './AgentStatisticTable';
import { format } from 'date-fns';

export interface AgentStatisticFetcherProps {
  configuration?: Configuration;
}

export const AgentStatisticFetcher = ({ configuration }: AgentStatisticFetcherProps) => {
  void configuration; // Not use configuration

  const { t } = useTranslation();
  const [load, setLoad] = React.useState(false);
  const [loadDate, setLoadDate] = React.useState<Date>();

  const { data, isLoading, refetch } = useGetAgentsStatistics(load);

  function handleClickLoad() {
    setLoad(true);
  }

  function handleReload() {
    refetch();
  }

  React.useEffect(() => {
    setLoadDate(new Date());
  }, [data]);

  return (
    <div className="flex flex-col h-full">
      <div className="flex gap-10">
        <h3 className="text-lg font-semibold">Agent statistic</h3>
      </div>
      <Separator className="my-6" />
      <div className="bg-red-300"></div>
      <div className="h-[-webkit-fill-available] relative overflow-hidden">
        {isLoading && (
          <div className="absolute flex items-center justify-center w-full h-full">
            <CgSpinner className="absolute opacity-100 animate-spin" size={100} />
            <div className="w-full h-full bg-gray-400 opacity-20"></div>
          </div>
        )}
        {data ? (
          <div className="flex flex-col h-full gap-5">
            <div className="flex flex-row items-center justify-end gap-1">
              {loadDate && format(loadDate, 'yyyy.MM.dd HH:mm:ss')}
              <Button onClick={handleReload} size="sm">
                <HiOutlineRefresh size={18} />
              </Button>
            </div>
            <AgentStatisticContainer data={data} />
            <AgentStatisticTable data={data} />
          </div>
        ) : (
          <div
            className={cn('flex flex-col items-center justify-center h-full gap-3', {
              'opacity-20': isLoading,
            })}
          >
            {t('CONFIGURATION.AGENT_STATISTIC.LOAD_GUIDE')}
            <Button className="w-max" onClick={handleClickLoad} disabled={isLoading}>
              {t('CONFIGURATION.AGENT_STATISTIC.LOADING')}
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};
