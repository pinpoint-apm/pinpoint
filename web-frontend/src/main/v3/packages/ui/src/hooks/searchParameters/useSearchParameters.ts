import { useLocation } from 'react-router-dom';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { useAtomValue } from 'jotai';
import { searchParametersAtom } from '@pinpoint-fe/ui/src/atoms';

export const useSearchParameters = () => {
  const props = useLocation();
  const atomSearchParameters = useAtomValue(searchParametersAtom);

  const searchParameters = Object.fromEntries(new URLSearchParams(props.search));
  const application = getApplicationTypeAndName(props.pathname);

  return {
    ...props,
    searchParameters: application ? searchParameters : atomSearchParameters?.searchParameters,
    application: application || atomSearchParameters?.application,
  };
};
