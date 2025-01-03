import { useGetErrorAnalysisTransactionInfoData } from '@pinpoint-fe/ui/hooks';
import { ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/constants';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../../ui';
import { ClipboardCopyButton } from '../../Button';
import { CollapsibleCodeViewer } from '../..';
import { format } from 'date-fns';

export interface ErrorAnalysisErrorDetailFetcherProps {
  errorInfo: ErrorAnalysisTransactionInfo.Parameters;
}

export const ErrorAnalysisErrorDetailFetcher = ({
  errorInfo,
}: ErrorAnalysisErrorDetailFetcherProps) => {
  const { data } = useGetErrorAnalysisTransactionInfoData(errorInfo);

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
          <Card key={i} className="rounded-md shadow-none">
            <CardHeader className="px-4 py-4">
              <CardTitle className="flex items-center">
                <ClipboardCopyButton
                  copyValue={d.errorClassName}
                  containerClassName="relative"
                  btnClassName="text-muted-foreground border-none shadow-none absolute -top-[4px] left-auto w-5 h-5"
                  hoverable
                >
                  {d.errorClassName}
                </ClipboardCopyButton>
                <span className="ml-auto text-xs font-normal text-muted-foreground">
                  {format(d.timestamp, 'HH:mm:ss SSS')}
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
