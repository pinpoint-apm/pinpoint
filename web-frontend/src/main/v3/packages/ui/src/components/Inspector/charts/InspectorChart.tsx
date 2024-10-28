import { HelpPopover } from '../../HelpPopover';
import { Card, CardHeader, CardTitle, CardContent, Separator } from '../../ui';
import { ChartCore } from './ChartCore';
import { ChartCoreProps } from './ChartCore';

export const HELP_VIEWER_KEY = {
  heap: 'HEAP',
  nonHeap: 'NON_HEAP',
  cpu: 'CPU_USAGE',
  apdex: 'APDEX_SCORE',
  transaction: 'TPS',
  activeTrace: 'ACTIVE_REQUEST',
  totalThreadCount: 'TOTAL_THREAD',
  responseTime: 'RESPONSE_TIME',
  fileDescriptor: 'OPEN_FILE_DESCRIPTOR',
  directCount: 'DIRECT_BUFFER_COUNT',
  directMemoryUsed: 'DIRECT_BUFFER_MEMORY',
  mappedMemoryCount: 'MAPPED_BUFFER_COUNT',
  mappedMemoryUsed: 'MAPPED_BUFFER_MEMORY',
  loadedClass: 'LOADED_CLASS_COUNT',
  unloadedClass: 'UNLOADED_CLASS_COUNT',
  jvmCpu: 'JVM_CPU_USAGE',
  systemCpu: 'SYSTEM_CPU_USAGE',
};

export interface InspectorChartProps extends ChartCoreProps {
  children?: React.ReactNode;
  helpViewerKey?: string;
}

export const InspectorChart = ({
  helpViewerKey,
  data,
  children,
  ...props
}: InspectorChartProps) => {
  return (
    <Card className="rounded-lg">
      <CardHeader className="px-4 py-3 text-sm">
        <CardTitle className="flex gap-1">
          {data?.title}
          {helpViewerKey && <HelpPopover helpKey={helpViewerKey} />}
        </CardTitle>
      </CardHeader>
      <Separator />
      <CardContent className="p-0 pb-1">
        <ChartCore data={data} className="aspect-video" {...props} />
        {children}
      </CardContent>
    </Card>
  );
};
