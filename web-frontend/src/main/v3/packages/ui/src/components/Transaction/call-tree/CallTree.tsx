import React from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { formatInTimeZone } from 'date-fns-tz';
import { useSetAtom } from 'jotai';
import { useTranslation } from 'react-i18next';
import { FaLink } from 'react-icons/fa';
import { PiStackDuotone } from 'react-icons/pi';
import { Link } from 'react-router';
import {
  APP_PATH,
  BASE_PATH,
  SEARCH_PARAMETER_DATE_FORMAT,
  TransactionInfoType as TransactionInfo,
} from '@pinpoint-fe/ui/src/constants';
import {
  convertParamsToQueryString,
  getErrorAnalysisPath,
  getTransactionDetailPath,
  getTransactionDetailQueryString,
  getTransactionListPath,
} from '@pinpoint-fe/ui/src/utils';
import {
  getTransactionTraceMetadataQueryOptions,
  usePostBind,
  useTimezone,
  useTransactionSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { transactionInfoCallTreeFocusId } from '@pinpoint-fe/ui/src/atoms';
import { Button } from '../..';
import { useReactToastifyToast } from '../../Toast';
import { CallTreeCore, CallTreeCoreProps } from './CallTreeCore';
import { OtelLinkArguments } from './callTreeTableColumns';

export interface CallTreeProps extends Omit<
  CallTreeCoreProps,
  'bindSql' | 'getErrorAnalysisHref' | 'renderOtelLink'
> {}

// Pinpoint web's Call Tree: `CallTreeCore` plus what needs the router and React Query — binding
// the SQL, and the links to the error analysis and to linked OTel transactions, which are built
// from this screen's URL.
export const CallTree = (props: CallTreeProps) => {
  const { metaData } = props;
  const { application, transactionInfo, pathname, searchParameters, serviceName } =
    useTransactionSearchParameters();
  const setCallTreeFocusId = useSetAtom(transactionInfoCallTreeFocusId);
  const [timezone] = useTimezone();
  const { mutateAsync: bindSql } = usePostBind();
  const focusTimestamp = transactionInfo?.focusTimestamp;

  const getErrorAnalysisHref = React.useCallback(
    (errorKey: TransactionInfo.ErrorKey) => {
      // The call stack's begin/end can be empty (0) for many nodes, which would
      // produce a window around epoch 0 and a negative `from` that the backend
      // (Timestamp) rejects. Center the window on the transaction's focus
      // timestamp, the same absolute time passed as `timestamp` below. Fall back
      // to the transaction's start time when focusTimestamp is missing (0), e.g.
      // when reached via an OTel link that carries no focus timestamp.
      const baseTime = focusTimestamp || metaData.callStackStart;
      const from = formatInTimeZone(baseTime - 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
      const to = formatInTimeZone(baseTime + 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
      return `${BASE_PATH}${getErrorAnalysisPath(application, undefined, serviceName)}?${convertParamsToQueryString(
        {
          from,
          to,
          transactionInfo: JSON.stringify({
            ...errorKey,
            timestamp: focusTimestamp,
            uriTemplate: metaData.uri,
          }),
          openErrorDetail: true,
        },
      )}`;
    },
    [application, serviceName, focusTimestamp, metaData, timezone],
  );

  const renderOtelLink = React.useCallback(
    (otelLink: OtelLinkArguments, text: string) => {
      const linkPath = buildOtelLinkPath(otelLink, application, {
        pathname,
        searchParameters,
        serviceName,
      });
      return (
        <>
          <Button asChild className="text-xs h-4 gap-0.5 p-1">
            <Link to={linkPath} onClick={() => setCallTreeFocusId('')}>
              <FaLink /> {text}
            </Link>
          </Button>
          <OtelLinkTransactionListButton traceId={otelLink.traceId} spanId={otelLink.spanId} />
        </>
      );
    },
    [application, pathname, searchParameters, serviceName, setCallTreeFocusId],
  );

  return (
    <CallTreeCore
      {...props}
      bindSql={bindSql}
      getErrorAnalysisHref={getErrorAnalysisHref}
      renderOtelLink={renderOtelLink}
    />
  );
};

const buildOtelLinkPath = (
  otelLink: OtelLinkArguments,
  application: Parameters<typeof getTransactionDetailPath>[0],
  context: {
    pathname: string;
    searchParameters: Record<string, string>;
    /** URL에 실려 있던 service 이름. 어느 화면으로 가든 다시 실어 헤더를 유지한다. */
    serviceName?: string;
  },
): string => {
  const transactionInfoParams = {
    agentId: '',
    spanId: otelLink.spanId,
    traceId: otelLink.traceId,
    focusTimestamp: otelLink.focusTimestamp,
    linkTraceId: otelLink.linkTraceId,
    linkSpanId: otelLink.linkSpanId,
  };

  // Stay on /transactionList when the user opened the link from the list page,
  // so the upper transaction-list panel (heatmap drag results) is preserved.
  const onTransactionListPage =
    context.pathname === APP_PATH.TRANSACTION_LIST ||
    context.pathname.startsWith(`${APP_PATH.TRANSACTION_LIST}/`);
  if (onTransactionListPage) {
    const path = getTransactionListPath(application, undefined, context.serviceName);
    return `${path}?${convertParamsToQueryString({
      ...context.searchParameters,
      transactionInfo: JSON.stringify(transactionInfoParams),
    })}`;
  }

  const path = getTransactionDetailPath(application, undefined, context.serviceName);
  return `${path}?${getTransactionDetailQueryString(transactionInfoParams)}`;
};

/**
 * Opens the OTel Link target transaction standalone in the Transaction List screen
 * (?traceInfo mode) in a new tab. Unlike the merged-view Link button, the target
 * application/time are unknown until the traceId is resolved, so this fetches
 * /api/transaction/metadata first and builds the application-scoped URL from the result.
 */
const OtelLinkTransactionListButton = ({
  traceId,
  spanId,
}: {
  traceId: string;
  spanId: string;
}) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const toast = useReactToastifyToast();
  const [timezone] = useTimezone();
  // 이 화면(transactionList)의 URL이 곧 service의 출처다. serviceName이 없으면 링크에도 싣지
  // 않고, 열린 화면이 전역 선택값으로 조회한다(`getRequestService`).
  const { serviceName } = useTransactionSearchParameters();

  const handleClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    // fetch errors already surface via the global query error toast
    const data = await queryClient
      .fetchQuery(getTransactionTraceMetadataQueryOptions(traceId))
      .catch(() => null);
    if (data === null) {
      return;
    }
    const rows = data?.metadata ?? [];

    // A trace may contain multiple spans (multi-application); prefer the span the
    // link points to, falling back to the first row (e.g. link target is a sub-span).
    const preferred = rows.find((row) => String(row.spanId) === spanId) || rows[0];
    if (!preferred?.applicationName || !preferred?.serviceType) {
      toast.warn(t('TRANSACTION_LIST.LINKED_TRANSACTION_NOT_FOUND'));
      return;
    }

    const baseTime = preferred.collectorAcceptTime;
    const from = formatInTimeZone(baseTime - 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
    const to = formatInTimeZone(baseTime + 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
    const path = getTransactionListPath(
      { applicationName: preferred.applicationName, serviceType: preferred.serviceType },
      undefined,
      serviceName,
    );
    const url = `${BASE_PATH}${path}?${convertParamsToQueryString({
      from,
      to,
      traceInfo: traceId,
      transactionInfo: JSON.stringify({
        agentId: preferred.agentId,
        spanId: preferred.spanId,
        traceId: preferred.traceId,
        focusTimestamp: preferred.collectorAcceptTime,
        path: preferred.application,
      }),
    })}`;
    // noopener/noreferrer prevents the opened tab from reaching back through
    // window.opener (reverse tab-nabbing). With noopener, open() returns null;
    // defensively null the opener too in case a browser ignores the feature.
    const opened = window.open(url, '_blank', 'noopener,noreferrer');
    if (opened) {
      opened.opener = null;
    }
  };

  return (
    <Button
      className="flex-none text-xs h-4 gap-0.5 p-1 ml-1"
      title={t('TRANSACTION_LIST.OPEN_IN_TRANSACTION_LIST')}
      aria-label={t('TRANSACTION_LIST.OPEN_IN_TRANSACTION_LIST')}
      onClick={handleClick}
    >
      <PiStackDuotone /> {t('TRANSACTION_LIST.JUMP')}
    </Button>
  );
};
