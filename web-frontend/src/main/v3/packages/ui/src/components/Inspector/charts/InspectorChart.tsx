import { Card, CardHeader, CardTitle, CardContent, Separator } from '../../ui';
import { ChartCore } from './ChartCore';
import { ChartCoreProps } from './ChartCore';

export interface InspectorChartProps extends ChartCoreProps {
  children?: React.ReactNode;
}

export const InspectorChart = ({ data, children, ...props }: InspectorChartProps) => {
  return (
    <Card className="rounded-lg">
      <CardHeader className="px-4 py-3 text-sm">
        <CardTitle>{data?.title}</CardTitle>
      </CardHeader>
      <Separator />
      <CardContent className="p-0 pb-1">
        <ChartCore data={data} className="aspect-video" {...props} />
        {children}
      </CardContent>
    </Card>
  );
};
