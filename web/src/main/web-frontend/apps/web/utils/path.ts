import { ApplicationType } from '@pinpoint-fe/ui';

export const getServerMapPath = ({ applicationName, serviceType}: ApplicationType) => {
  return `/serverMap/${applicationName}@${serviceType}`
}