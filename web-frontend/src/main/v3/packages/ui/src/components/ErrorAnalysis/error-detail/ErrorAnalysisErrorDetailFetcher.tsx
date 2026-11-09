import React from 'react';
import {
  useGetErrorAnalysisTransactionInfoData,
  useRequestService,
  useTimezone,
} from '@pinpoint-fe/ui/src/hooks';
import { ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { ErrorDetailFocus, findErrorDetailFocusIndex } from '@pinpoint-fe/ui/src/utils';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../../ui';
import { ClipboardCopyButton } from '../../Button';
import { CollapsibleCodeViewer } from '../..';
import { cn } from '../../../lib';
import { formatInTimeZone } from 'date-fns-tz';

export interface ErrorAnalysisErrorDetailFetcherProps {
  errorInfo: ErrorAnalysisTransactionInfo.Parameters;
  /** 눌러서 온 예외. 주면 그 카드로 스크롤하고 표시한다(#14203). 없으면 맨 위에서 시작한다. */
  focus?: ErrorDetailFocus;
}

export const ErrorAnalysisErrorDetailFetcher = ({
  errorInfo,
  focus,
}: ErrorAnalysisErrorDetailFetcherProps) => {
  const [timezone] = useTimezone();
  // TODO #14196: drop the screen-service fallback once list responses carry serviceName
  const screenServiceName = useRequestService();
  const requestServiceName = errorInfo.serviceName ?? screenServiceName;
  const { data } = useGetErrorAnalysisTransactionInfoData({
    ...errorInfo,
    serviceName: requestServiceName,
  });
  const focusIndex = findErrorDetailFocusIndex(data ?? [], focus);
  const cardRefs = React.useRef<(HTMLDivElement | null)[]>([]);

  // 열릴 때와 다른 예외로 바뀔 때만 스크롤한다. 같은 대상의 재조회(data만 새 객체)에는 다시 끌어오지
  // 않도록 deps를 원시 값으로 둔다 — 사용자가 내려 둔 스크롤을 빼앗지 않는다.
  React.useEffect(() => {
    if (focusIndex !== -1) {
      cardRefs.current[focusIndex]?.scrollIntoView({ block: 'start' });
    }
  }, [
    focusIndex,
    errorInfo.transactionId,
    errorInfo.spanId,
    errorInfo.exceptionId,
    focus?.exceptionDepth,
    focus?.errorClassName,
    focus?.errorMessage,
  ]);

  return (
    <div className="p-5 space-y-5 overflow-auto">
      {data?.map((d, i) => {
        const stackTrace = d.stackTrace
          .map(
            ({ className, methodName, fileName, lineNumber }) =>
              `at ${className}: ${methodName} (${fileName}: ${lineNumber})`,
          )
          .join('\n');

        return (
          <Card
            key={i}
            ref={(el) => {
              cardRefs.current[i] = el;
            }}
            // scroll-mt: 카드 테두리가 스크롤 영역 위끝에 붙지 않게 목록의 바깥 여백(p-5)만큼 띄운다.
            className={cn(
              'rounded-md shadow-none scroll-mt-5',
              i === focusIndex && 'border-status-fail',
            )}
            aria-current={i === focusIndex ? 'true' : undefined}
          >
            <CardHeader className="px-4 py-4">
              <CardTitle className="flex items-center">
                <ClipboardCopyButton
                  copyValue={d.errorClassName}
                  containerClassName="relative"
                  btnClassName="text-muted-foreground border-none shadow-none absolute top-[-4px] left-auto w-5 h-5"
                  hoverable
                >
                  {d.errorClassName}
                </ClipboardCopyButton>
                <span className="ml-auto text-xs font-normal text-muted-foreground">
                  {formatInTimeZone(d.timestamp, timezone, 'HH:mm:ss SSS')}
                </span>
              </CardTitle>
              <CardDescription>
                <ClipboardCopyButton
                  copyValue={d.errorMessage}
                  btnClassName="text-muted-foreground border-none shadow-none w-5 h-5"
                  hoverable
                >
                  {d.errorMessage}
                </ClipboardCopyButton>
              </CardDescription>
            </CardHeader>
            <CardContent className="px-4 pb-4">
              <CollapsibleCodeViewer title="Stack Trace" code={stackTrace} language="java" />
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
};
