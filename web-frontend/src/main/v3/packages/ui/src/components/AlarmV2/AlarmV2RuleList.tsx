import { ErrorBoundary } from '@pinpoint-fe/ui';
import { AlarmV2RuleListFetcher, AlarmV2RuleListFetcherProps } from './AlarmV2RuleListFetcher';

export type AlarmV2RuleListProps = AlarmV2RuleListFetcherProps;

export const AlarmV2RuleList = (props: AlarmV2RuleListProps) => {
  return (
    <ErrorBoundary resetKeys={[props.applicationName, props.applicationType]}>
      <AlarmV2RuleListFetcher {...props} />
    </ErrorBoundary>
  );
};
