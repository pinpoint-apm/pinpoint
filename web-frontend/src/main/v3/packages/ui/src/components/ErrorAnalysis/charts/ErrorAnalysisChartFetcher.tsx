import { useGetErrorAnalysisChartData } from '@pinpoint-fe/ui/src/hooks';
import { ErrorAnalysisChartCore, ErrorAnalysisChartCoreProps } from './ErrorAnalysisChartCore';

export interface ErrorAnalysisChartFetcherProps extends Omit<ErrorAnalysisChartCoreProps, 'data'> {}

export const ErrorAnalysisChartFetcher = (props: ErrorAnalysisChartFetcherProps) => {
  const { data } = useGetErrorAnalysisChartData();

  return <ErrorAnalysisChartCore data={data} {...props} />;
};
