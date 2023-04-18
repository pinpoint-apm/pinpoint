import React from 'react';
import useSWR from 'swr';

import { SERVER_MAP_DATA_V2 } from '@pinpoint-fe/constants';
import { ApplicationType } from '@pinpoint-fe/ui';
import { ServerMap as ServerMapComponent, Node, Edge } from '@pinpoint-fe/server-map';

export interface ServerMapProps {
  application: ApplicationType,
  dateRange: {
    from: Date;
    to: Date;
  }
}

export const ServerMap = ({
  application,
  dateRange
}: ServerMapProps) => {
  const { data } = useSWR<ServerMapInfo>(application && dateRange ? ([`${SERVER_MAP_DATA_V2}`, {
    applicationName: application?.applicationName,
    serviceTypeName: application?.serviceType,
    from: dateRange.from.getTime(),
    to: dateRange.to.getTime(),
    calleeRange: 1,
    callerRange: 1,
    wasOnly: false,
    bidirectional: false,
    useStatisticsAgentState: false,
  }]) : null);

  const getTransactionInfo = (node: NodeInfo) => {
    const { isWas, isAuthorized } = node;

    if (isWas && isAuthorized) {
      return {
        good: ['1s', '3s', '5s'].reduce((prev, curr) => {
          return prev + node?.histogram?.[curr]!;
        }, 0),
        slow: node.histogram?.Slow!,
        bad: node.histogram?.Error!,
      }
    }

  }

  const getServerMapData = (): {
    nodes: Node[],
    edges: Edge[],
  } => {
    const { nodeDataArray = [], linkDataArray = [] } = data?.applicationMapData || {};
    const nodes = nodeDataArray.map((node) => {
      return {
        id: node.key,
        label: node.applicationName,
        type: node.serviceType,
        imgPath: `/assets/img/servers/${node.serviceType}.png`,
        transactionInfo: getTransactionInfo(node),
        shouldNotMerge: () => {
          return node.isWas || node.serviceType === 'USER'
        }
      };
    });

    const edges = linkDataArray.map((link, i) => ({
      id: link.key,
      source: link.from,
      target: link.to,
    }));

    return {
      nodes,
      edges,
    }
  }

  const renderEdgeLabel = (edge: any) => {
    const edgeIdMap = data?.applicationMapData.linkDataArray.reduce((acc, curr) => {
      return {
        ...acc,
        [curr.key]: curr
      }
    }, {});

    if (edge.edges) {
      return `${edge.edges.reduce((acc, curr) => {
        return acc + edgeIdMap[curr.id]?.totalCount;
      }, 0)}`
    } else {
      return `${edgeIdMap[edge.id]?.totalCount}`
    }
  }

  return (
    <ServerMapComponent
      // data={{ nodes: [], edges: []}}
      data={getServerMapData()}
      baseNodeId={`${application?.applicationName}^${application?.serviceType}`}
      // renderEdgeLabel={renderEdgeLabel}
    />
  );
};
