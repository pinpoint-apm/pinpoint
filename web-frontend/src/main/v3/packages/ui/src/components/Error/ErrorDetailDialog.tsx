import React from 'react';
import {
  Button,
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
  Dialog,
  DialogContent,
  DialogTrigger,
  Separator,
} from '../ui';
import { ErrorLike } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../lib';
import { HighLightCode } from '../HighLightCode';
import { RxChevronDown, RxChevronUp } from 'react-icons/rx';

export interface ErrorDetailDialogProps {
  error: Error | ErrorLike;
  contentOption?: React.ComponentPropsWithoutRef<typeof DialogContent>;
  contentClassName?: string;
}

function getDisplayMessage(error: Error | ErrorLike): string {
  const msg = (error as Error).message;
  const detail = (error as ErrorLike).detail;
  return detail ?? msg ?? '';
}

function getClientStack(error: Error | ErrorLike): string | null {
  const stack = (error as Error).stack;
  return stack ?? null;
}

function getServerTrace(error: Error | ErrorLike): string | null {
  const trace = (error as ErrorLike).trace;
  if (Array.isArray(trace) && trace.length > 0) return trace.join('\n');
  if (typeof trace === 'string') return trace;
  return null;
}

export const ErrorDetailDialog = ({
  error,
  contentOption,
  contentClassName,
}: ErrorDetailDialogProps) => {
  const [serverTraceOpen, setServerTraceOpen] = React.useState(false);
  const serverError = error as ErrorLike;
  const hasMethod = serverError?.method != null;
  const hasStatus = serverError?.status != null;
  const params = serverError?.parameters ?? {};
  const hasParams = Object.keys(params).length > 0;
  const clientStack = getClientStack(error);
  const serverTrace = getServerTrace(error);

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button className="p-0 text-xs" variant="link" onClick={(e) => e.stopPropagation()}>
          Show details...
        </Button>
      </DialogTrigger>
      <DialogContent
        className={cn('max-h-[90%] overflow-auto max-w-5xl', contentClassName)}
        onMouseDown={(e) => e.stopPropagation()}
        {...contentOption}
      >
        <div className="flex overflow-hidden flex-col gap-4">
          <div className="space-y-2">
            <h4 className="flex gap-1 items-center font-medium">
              <div className="w-1 h-4 rounded-sm bg-status-fail" />
              Error Details
            </h4>

            {serverError?.instance && (
              <div className="flex gap-1 items-center">
                {serverError?.url ? (
                  <a
                    className="text-sm font-semibold text-primary hover:underline"
                    href={serverError.url}
                    target="_blank"
                    rel="noreferrer"
                  >
                    {serverError.instance}
                  </a>
                ) : (
                  <span className="text-sm font-semibold">
                    {serverError.instance}
                  </span>
                )}
              </div>
            )}
            {hasMethod && (
              <div className="grid grid-cols-[5rem_auto] gap-x-2 gap-y-1 text-sm items-baseline">
                <div className="text-muted-foreground">Method</div>
                <div>{serverError.method}</div>
              </div>
            )}
            {hasStatus && (
              <div className="grid grid-cols-[5rem_auto] gap-x-2 gap-y-1 text-sm items-baseline">
                <div className="text-muted-foreground">Status</div>
                <div>{serverError.status}</div>
              </div>
            )}
            {serverError.title != null && serverError.title !== '' && (
              <div className="grid grid-cols-[5rem_auto] gap-x-2 gap-y-1 text-sm items-baseline">
                <div className="text-muted-foreground">Title</div>
                <div>{serverError.title}</div>
              </div>
            )}
            {serverError.detail != null && serverError.detail !== '' && (
              <div className="grid grid-cols-[5rem_auto] gap-x-2 gap-y-1 text-sm items-baseline">
                <div className="text-muted-foreground">Detail</div>
                <p className="text-sm break-all">{getDisplayMessage(error) || '—'}</p>
              </div>
            )}
            {!hasStatus && (
              <p className="text-sm break-all text-muted-foreground">
                {getDisplayMessage(error) || '—'}
              </p>
            )}
            {hasParams && (
              <>
                <div className="grid gap-2 text-sm scrollbar-hide">
                  {hasParams && (
                    <>
                      <div className="text-muted-foreground">Parameters</div>
                      <div className="grid grid-cols-[10rem_auto] gap-2 text-xs">
                        {Object.keys(params)
                          .sort()
                          .map((key) => {
                            const value = params[key];
                            return (
                              <React.Fragment key={key}>
                                <div className="pl-3 text-muted-foreground">{key}</div>
                                <div className="pl-3 break-all">
                                  {Array.isArray(value) ? value.join(', ') : String(value)}
                                </div>
                              </React.Fragment>
                            );
                          })}
                      </div>
                    </>
                  )}
                </div>
              </>
            )}
          </div>
          {clientStack != null && (
            <>
              <Separator />
              <div className="overflow-auto">
                <pre className="p-2 text-sm">{clientStack}</pre>
              </div>
            </>
          )}
          {serverTrace != null && (
            <>
              <Separator />
              <Collapsible
                className="space-y-2"
                open={serverTraceOpen}
                onOpenChange={setServerTraceOpen}
                defaultOpen={false}
              >
                <CollapsibleTrigger
                  className="flex gap-1 items-center text-sm text-muted-foreground hover:text-foreground"
                  onClick={(e) => e.stopPropagation()}
                >
                  Server stack trace {serverTraceOpen ? <RxChevronUp /> : <RxChevronDown />}
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <HighLightCode
                    language="java"
                    code={serverTrace}
                    className="overflow-auto p-2 text-xs"
                  />
                </CollapsibleContent>
              </Collapsible>
            </>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};
