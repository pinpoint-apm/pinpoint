import React from 'react';
import { BASE_PATH } from '@pinpoint-fe/constants';
import { useGetTransactionInfo } from '@pinpoint-fe/hooks';
import { useSetAtom } from 'jotai';
import { useTranslation } from 'react-i18next';
import { FaChevronRight } from 'react-icons/fa6';
import { Button, ServerMapCore, Tabs, TabsContent, TabsList, TabsTrigger } from '../..';
import { CallTree } from '..';
import {
  getBaseNodeId,
  getServerIconPath,
  getTransactionDetailPath,
  getTransactionDetailQueryString,
} from '@pinpoint-fe/utils';
import { useTransactionSearchParameters } from '@pinpoint-fe/hooks';
import { RxExternalLink } from 'react-icons/rx';
import { transactionInfoDatasAtom } from '@pinpoint-fe/atoms';
import { cn } from '../../../lib';
import { Popover, PopoverContent, PopoverTrigger } from '../../ui';

export interface TransactionInfoFetcherProps {
  disableHeader?: boolean;
}

const tabList = [
  { id: 'callTree', display: 'Call Tree' },
  { id: 'serverMap', display: 'Server Map' },
];

export const TransactionInfoFetcher = ({ disableHeader }: TransactionInfoFetcherProps) => {
  const { application, transactionInfo } = useTransactionSearchParameters();
  const { data, tableData, mapData } = useGetTransactionInfo();
  const setTransactionInfo = useSetAtom(transactionInfoDatasAtom);
  const { t } = useTranslation();

  React.useEffect(() => {
    setTransactionInfo(data);
  }, [data]);

  if (!data) {
    return (
      <div className="flex items-center justify-center h-full">
        {t('TRANSACTION_LIST.SELECT_TRANSACTION')}
      </div>
    );
  }

  return (
    <Tabs defaultValue="callTree" className="h-full">
      <div className="p-3 border-b">
        {!disableHeader && (
          <div className="flex items-center gap-1 pb-2 text-sm font-semibold truncate">
            <img
              style={{ width: '0.875rem' }}
              height="auto"
              alt={'application image'}
              src={getServerIconPath(application!)}
            />
            <div className="truncate">{application?.applicationName}</div>
            <div className="truncate">({data.agentId})</div>
            <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
            <div className="truncate">{data.applicationName}</div>
            <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
            <div className="truncate text-muted-foreground">{data.transactionId}</div>
            <Button
              className="ml-auto w-8 h-8 text-base text-muted-foreground py-0.5 px-1"
              variant="ghost"
              onClick={() => {
                window.open(
                  `${BASE_PATH}${getTransactionDetailPath(
                    application,
                  )}?${getTransactionDetailQueryString({
                    agentId: data.agentId,
                    traceId: data.transactionId,
                    spanId: transactionInfo.spanId,
                    focusTimestamp: transactionInfo.focusTimestamp,
                  })}`,
                );
              }}
            >
              <RxExternalLink />
            </Button>
          </div>
        )}

        <div className="flex items-center">
          <TabsList>
            {tabList.map((tab) => (
              <TabsTrigger className="text-xs" key={tab.id} value={tab.id}>
                {tab.display}
              </TabsTrigger>
            ))}
          </TabsList>

          <Popover>
            <PopoverTrigger asChild>
              <Button
                size="sm"
                variant="link"
                disabled={!data.logLinkEnable}
                onClick={(e) => {
                  if (data.loggingTransactionInfo) {
                    e.preventDefault();
                    window.open(
                      `${location.protocol}//${location.host}/${encodeURI(data.logPageUrl)}`,
                    );
                  }
                }}
              >
                {data.logButtonName}
              </Button>
            </PopoverTrigger>
            <PopoverContent
              className="space-y-2 text-xs [&_a]:text-primary [&_a]:hover:underline"
              dangerouslySetInnerHTML={{ __html: data.disableButtonMessage }}
            ></PopoverContent>
          </Popover>
        </div>
      </div>
      {tabList.map((tab) => {
        let Content;
        if (tab.id === 'callTree') {
          Content = <CallTree data={tableData} metaData={data} mapData={mapData || []} />;
        } else if (tab.id === 'serverMap' && data) {
          Content = (
            <ServerMapCore
              // eslint-disable-next-line @typescript-eslint/ban-ts-comment
              // @ts-ignore
              data={data}
              baseNodeId={getBaseNodeId({
                application: application,
                // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                // @ts-ignore
                applicationMapData: data.applicationMapData,
              })}
              disableMenu
            />
          );
        }
        return (
          <TabsContent
            key={tab.id}
            value={tab.id}
            className={cn('mt-0 h-[calc(100%-5.5625rem)]', {
              'h-[calc(100%-3.75rem)]': disableHeader,
            })}
          >
            {Content}
          </TabsContent>
        );
      })}
    </Tabs>
  );
};
