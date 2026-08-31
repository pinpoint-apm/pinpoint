import { useLocation } from 'react-router';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';

export const useSearchParameters = () => {
  const props = useLocation();

  const searchParameters = Object.fromEntries(new URLSearchParams(props.search));
  const application = getApplicationTypeAndName(props.pathname);

  return { ...props, searchParameters, application };
};
