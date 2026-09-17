import { ErrorBoundary } from '@pinpoint-fe/ui';
import {
  AlarmV2TemplateListFetcher,
  AlarmV2TemplateListFetcherProps,
} from './AlarmV2TemplateListFetcher';

export type AlarmV2TemplateListProps = AlarmV2TemplateListFetcherProps;

export const AlarmV2TemplateList = ({ ...props }: AlarmV2TemplateListProps) => {
  return (
    <ErrorBoundary>
      <AlarmV2TemplateListFetcher {...props} />
    </ErrorBoundary>
  );
};
