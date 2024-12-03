import { Widget } from '../../../components/Dashboard/Widget';
import { OpenTelemetryMetric } from '../charts';
import { useInView } from 'react-intersection-observer';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';

export function OpenTelemetryWidget({
  metric,
  applicationName,
  onDelete,
  onEdit,
}: {
  metric: OtlpMetricDefUserDefined.Metric;
  applicationName: string;
  onEdit?: (metric: OtlpMetricDefUserDefined.Metric) => void;
  onDelete?: (metric: OtlpMetricDefUserDefined.Metric) => void;
}) {
  const { ref, inView } = useInView({
    initialInView: false,
    threshold: 0.1,
  });

  return (
    <div ref={ref}>
      <Widget
        title={metric.title}
        onClickDelete={() => {
          onDelete?.(metric);
        }}
        onClickEdit={() => {
          onEdit?.(metric);
        }}
      >
        <OpenTelemetryMetric
          inView={inView}
          metricDefinition={metric}
          dashboardId={applicationName}
        />
      </Widget>
    </div>
  );
}
