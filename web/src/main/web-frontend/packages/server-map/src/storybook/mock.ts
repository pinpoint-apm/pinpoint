import { Node, Edge } from '../types'

export const data: {
  applicationMapData: {
    range: any,
    nodeDataArray: any,
    linkDataArray: any,
  }
} = {
}

const getTransactionInfo = (node: any) => {
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

export const getServerMapData = (): {
  nodes: Node[],
  edges: Edge[],
} => {
  const { nodeDataArray = [], linkDataArray = [] } = data?.applicationMapData!;
  const nodes = nodeDataArray.map((node: any) => {
    return {
      id: node.key,
      label: node.applicationName,
      type: node.serviceType,
      imgPath: `/assets/img/servers/${node.serviceType}.png`,
      transactionInfo: getTransactionInfo(node),
    };
  });

  const edges = linkDataArray.map((link: any, i: number) => ({
    id: link.key,
    source: link.from,
    target: link.to,
    transactionInfo: {
      totalCount: link.totalCount,
    },
  }));

  return {
    nodes,
    edges,
  }
}