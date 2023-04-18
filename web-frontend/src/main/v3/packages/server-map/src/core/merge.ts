import _ from 'lodash';
import { Node, Edge, MergedNode, MergedEdge } from '../types';
import { getTransactionStatusSVGString } from '../ui/template/node';
type GroupBySource = { [key: string]: string[] }

const mergeNodes = ({ nodes, edges }: { nodes: Node[], edges: Edge[] }) => {
  const { single, multi } = groupEndNodesByTargetCount(nodes, edges);

}

const mergeSingleTargetedNodes = (singles: string[], edges: Edge[]) => {
  const a = groupSingleTargetsBySource(singles, edges);
}

const mergeMultiTargetedNodes = (multies: string[]) => {

}

// { single: [t4, t5], multi: [t1, t2, t3] }
const groupEndNodesByTargetCount = (nodes: Node[], edges: Edge[]) => {
  const targetNodeIds = edges.map(edge => edge.target);

  // [ t1, t1, t1, t2, t2, t3, t3, t3, t3, t4, t5 ]
  const EndNodesIds = targetNodeIds.filter(id => {
    return !edges.some(edge => edge.source === id);
  });

  const shouldMergeNodeIds = EndNodesIds.filter(id => {
    return !(nodes.find(node => id === node.id)?.shouldNotMerge?.());
  })

  // { t1: 3, t2: 2, t3: 4, t4: 1, t5: 1 }
  const targetIdAndCount = shouldMergeNodeIds.reduce<{ [key: string]: number }>((prev, curr) => {
    if (prev[curr]) {
      return { ...prev, [curr]: prev[curr] + 1 }
    } else {
      return { ...prev, [curr]: 1 }
    }
  }, {});

  return Object.entries(targetIdAndCount)
    .reduce<{ single: string[], multi: string[] }>((prev, [key, count]) => {
      const arr = { ...prev }
      count > 1 ? arr.multi.push(key) : arr.single.push(key)
      return arr
    }, { single: [], multi: [] });
}

// { source1: [t4, t5], source2: [t1, t2], source: [t3]}
const groupSingleTargetsBySource = (singleTargets: string[], edges: Edge[]) => {
  return singleTargets.reduce<{ [key: string]: string[] }>((prev, curr) => {
    const { source } = edges.find(edge => edge.target === curr)!;

    if (prev[source]) {
      prev[source].push(curr);
    } else {
      prev[source] = [curr];
    }
    return prev;
  }, {});
}

// { source1: [t4, t5], source2: [t1, t2], source: [t3]}
const groupMultiTargetsBySource = (multiTargets: string[], edges: Edge[]) => {
  return multiTargets.reduce<{ [key: string]: string[] }>((prev, curr) => {
    const sourcesKey = edges.filter(edge => edge.target === curr).map(edge => edge.source).sort().toString();

    if (prev[sourcesKey]) {
      prev[sourcesKey].push(curr);
    } else {
      prev[sourcesKey] = [curr];
    }
    return prev;
  }, {})
}

// { source1: { type1: [t4, t5] }, source2: { type:2 [t1]} ...}
const groupByType = (groupBySource: GroupBySource, nodes: Node[]) => {
  return Object.entries(groupBySource).reduce<{ [key: string]: { [key: string]: string[] } }>((prev, [source, targetIds]) => {
    const result = { ...prev };

    targetIds.forEach(id => {
      const node = nodes.find(n => n.id === id);

      if (result[source]) {
        if (node?.type && result[source][node.type]) {
          result[source][node.type].push(id);
        } else if (node?.type) {
          result[source][node.type] = [id];
        }
      } else {
        if (node?.type) {
          result[source] = {};
          result[source][node.type] = [id];
        }
      }
    })
    return result;
  }, {})
}

export const getServerMapData = (data: { nodes: Node[], edges: Edge[] }) => {
  const { edges, nodes } = data
  const targetNodeIds = edges.map(edge => edge.target);

  // [ t1, t1, t1, t2, t2, t3, t3, t3, t3, t4, t5 ]
  const leafNodesIds = targetNodeIds.filter(id => {
    return !edges.some(edge => edge.source === id);
  });

  const shouldMergeNodeIds = leafNodesIds.filter(id => {
    return !(nodes.find(node => id === node.id)?.shouldNotMerge?.());
  })

  // { t1: 3, t2: 2, t3: 4, t4: 1, t5: 1 }
  const targetIdAndCount = shouldMergeNodeIds.reduce<{ [key: string]: number }>((prev, curr) => {
    if (prev[curr]) {
      return { ...prev, [curr]: prev[curr] + 1 }
    } else {
      return { ...prev, [curr]: 1 }
    }
  }, {});

  // { single: [t4, t5], multi: [t1, t2, t3] }
  const groupByTargetCount = Object.entries(targetIdAndCount)
    .reduce<{ single: string[], multi: string[] }>((prev, [key, count]) => {
      const arr = { ...prev }
      count > 1 ? arr.multi.push(key) : arr.single.push(key)
      return arr
    }, { single: [], multi: [] });

  // { source1: [t4, t5], source2: [t1, t2], source: [t3]}
  const groupBySourceOnSingle = groupByTargetCount.single.reduce<{ [key: string]: string[] }>((prev, curr) => {
    const { source } = edges.find(edge => edge.target === curr)!;

    if (prev[source]) {
      prev[source].push(curr);
    } else {
      prev[source] = [curr];
    }
    return prev;
  }, {});

  // { source1: [t4, t5], source2: [t1, t2], source: [t3]}
  const groupBySourceOnMulti = groupByTargetCount.multi.reduce<{ [key: string]: string[] }>((prev, curr) => {
    const sourcesKey = edges.filter(edge => edge.target === curr).map(edge => edge.source).sort().toString();

    if (prev[sourcesKey]) {
      prev[sourcesKey].push(curr);
    } else {
      prev[sourcesKey] = [curr];
    }
    return prev;
  }, {});

  // { source1: { type1: [t4, t5] }, source2: { type:2 [t1]} ...}
  const getGroupByType = (groupBySource: GroupBySource) => Object.entries(groupBySource).reduce<{ [key: string]: { [key: string]: string[] } }>((prev, [source, targetIds]) => {
    const result = { ...prev };

    targetIds.forEach(id => {
      const node = nodes.find(node => node.id === id);

      if (result[source]) {
        if (node?.type && result[source][node.type]) {
          result[source][node.type].push(id);
        } else if (node?.type) {
          result[source][node.type] = [id];
        }
      } else {
        if (node?.type) {
          result[source] = {};
          result[source][node.type] = [id];
        }
      }
    })
    return result;
  }, {})

  const groupByTypeOnSingle = getGroupByType(groupBySourceOnSingle);
  const groupByTypeOnMulti = getGroupByType(groupBySourceOnMulti);


  const mergeSingleNodes = () => {
    // Node edges
    let mergedNodes: MergedNode[] = [...nodes]
    let mergedEdges: MergedEdge[] = [...edges]

    Object.entries(groupByTypeOnSingle).forEach(([source, typeAndTargetIds]) => {
      Object.entries(typeAndTargetIds).forEach(([type, targetIds]) => {
        if (targetIds.length > 1) {
          const id = `${source}_${type}_MergeSingleNodesByServerMap`;
          const imgPath = mergedNodes.find(node => node.id === targetIds[0])?.imgPath;

          const [notToMergeNodes, toMergeNodes] = _.partition(mergedNodes, node => !targetIds.includes(node.id));

          mergedNodes = [...notToMergeNodes, {
            id,
            imgPath,
            label: `total: ${targetIds.length}`,
            nodes: toMergeNodes,
          }];

          const [notToMergeEdge, toMergeEdge] = _.partition(mergedEdges, edge => !targetIds.includes(edge.target));
          mergedEdges = [...notToMergeEdge, {
            source,
            target: id,
            id: `${source}_${type}_MergeSingleEdgesByServerMap`,
            edges: toMergeEdge,
          }];
        }
      })
    })

    return {
      nodes: mergedNodes,
      edges: mergedEdges,
    }
  }

  const mergedData = mergeSingleNodes();

  const mergeMultieNodes = () => {
    // Node edges
    let mergedNodes: MergedNode[] = [...mergedData.nodes]
    let mergedEdges: MergedEdge[] = [...mergedData.edges]

    Object.entries(groupByTypeOnMulti).forEach(([source, typeAndTargetIds]) => {
      Object.entries(typeAndTargetIds).forEach(([type, targetIds]) => {
        if (targetIds.length > 1) {
          const id = `${source}_${type}_MergeMultiNodesByServerMap`;
          const imgPath = mergedNodes.find(node => node.id === targetIds[0])?.imgPath;
          const [notToMergeNodes, toMergeNodes] = _.partition(mergedNodes, node => !targetIds.includes(node.id));
          mergedNodes = [...notToMergeNodes, {
            id,
            imgPath,
            label: `total: ${targetIds.length}`,
            nodes: toMergeNodes,
          }];

          const sources = source.split(',');
          //[
          //   e1,e2,e3, => e10 (same source)
          //   e4,e5,e6, => e11 (same source)
          //   e7,e8,e9 => e12 (same source)
          // ]
          const [notToMergeEdge, toMergeEdge] = _.partition(mergedEdges, edge => !(sources.includes(edge.source) && targetIds.includes(edge.target)));
          const newEdges = toMergeEdge.reduce<{ [key: string]: MergedEdge }>((prev, curr, i) => {
            if (prev[curr.source]) {
              prev[curr.source].edges!.push(curr);
            } else {
              prev[curr.source] = {
                target: id,
                source: curr.source,
                id: `${curr.source}_${type}_MergeMultiEdgesByServerMap`,
                edges: [curr],
              }
            }
            return { ...prev };
          }, {});

          mergedEdges = [
            ...notToMergeEdge,
            ...Object.values(newEdges),
          ]
        }
      })
    })

    return {
      nodes: mergedNodes,
      edges: mergedEdges,
    }
  }

  const finalData = mergeMultieNodes();

  return [
    ...finalData.nodes.map(node => {
      return {
        data: {
          ...node,
          imgArr: [
            node?.imgPath,
            getTransactionStatusSVGString(node),
          ],
        }
      }
    }),
    ...finalData.edges.map(edge => ({
      data: {
        ...edge,
      }
    })),
  ]
}