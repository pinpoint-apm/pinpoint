import { ApplicationType, FilteredMap, GetServerMap } from '@pinpoint-fe/constants';

export const getBaseNodeId = ({
  application,
  applicationMapData,
}: {
  application: ApplicationType | null;
  applicationMapData?: GetServerMap.ApplicationMapData | FilteredMap.ApplicationMapData;
}) => {
  if (application && applicationMapData) {
    const nodeList = applicationMapData.nodeDataArray;
    const baseNodeId = `${application?.applicationName}^${application?.serviceType}`;

    return nodeList.length === 0 || nodeList.some(({ key }: { key: string }) => key === baseNodeId)
      ? baseNodeId
      : baseNodeId.replace(/(.*)\^(.*)/i, '$1^UNAUTHORIZED');
  }
  return '';
};
