import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

export const getApplicationTypeAndName = (path = '') => {
  const splittedPath = path.match(/\/?([^/]+)[@|^]([^/]+)$/);
  const applicationName = splittedPath?.[1];
  const serviceType = splittedPath?.[2];

  if (applicationName && serviceType) {
    return { applicationName, serviceType };
  }

  return null;
};

export const getApplicationKey = (application?: ApplicationType) => {
  return `${application?.applicationName}^${application?.serviceType}`;
};
