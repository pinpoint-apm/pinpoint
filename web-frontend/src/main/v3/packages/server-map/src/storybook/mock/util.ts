import { Node, Edge } from '../../types';

const getTransactionInfo = (node: any) => {
  const { isWas, isAuthorized } = node;

  if (isWas && isAuthorized) {
    return {
      // Node.transactionInfo declares these as number, so fall back to 0 rather than
      // letting a missing histogram produce NaN/undefined.
      good: ['1s', '3s', '5s'].reduce((prev, curr) => {
        return prev + (node?.histogram?.[curr] ?? 0);
      }, 0),
      slow: node.histogram?.Slow ?? 0,
      bad: node.histogram?.Error ?? 0,
    };
  }
};

export const getServerMapData = (data: {
  applicationMapData: {
    range: any;
    nodeDataArray: any;
    linkDataArray: any;
  };
}): {
  nodes: Node[];
  edges: Edge[];
} => {
  const { nodeDataArray = [], linkDataArray = [] } = data.applicationMapData;
  const nodes = nodeDataArray.map((node: any) => {
    return {
      id: node.key,
      label: node.applicationName,
      type: node.serviceType,
      imgPath: `/assets/img/servers/${node.serviceType}.png`,
      transactionInfo: getTransactionInfo(node),
    };
  });

  const edges = linkDataArray.map((link: any) => ({
    id: link.key,
    source: link.from,
    target: link.to,
    transactionInfo: {
      totalCount: link.totalCount,
      avg: link.responseStatistics.Avg,
    },
  }));

  return {
    nodes,
    edges,
  };
};
