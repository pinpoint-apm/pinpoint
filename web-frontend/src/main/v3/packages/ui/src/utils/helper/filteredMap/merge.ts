import { FilteredMapType as FilteredMap } from '@pinpoint-fe/ui/src/constants';

export const mergeFilteredMapNodeData = (
  current: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.NodeData;
  },
  newNode: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.NodeData;
  },
) => {
  const acc = current?.data;
  const neo = newNode?.data;

  acc.hasAlert = neo.hasAlert;
  acc.slowCount += neo.slowCount;
  acc.errorCount += neo.errorCount;
  acc.totalCount += neo.totalCount;
  acc.instanceCount = Math.max(acc.instanceCount, neo.instanceCount);
  acc.instanceErrorCount = Math.max(acc.instanceErrorCount, neo.instanceErrorCount);

  mergeAgentIds(acc, neo);
  // mergeNodeAgentIdNameMap(acc, neo);
  mergeHistogram(acc, neo);
  mergeResponseStatistics(acc, neo);
  mergeAgentHistogram(acc, neo);
  mergeTimeSeriesHistogram(current, newNode);
  mergeAgentTimeSeriesHistogramByType(
    {
      timestamp: current?.timestamp,
      data: acc?.agentTimeSeriesHistogram,
    },
    {
      timestamp: newNode?.timestamp,
      data: neo?.agentTimeSeriesHistogram,
    },
  );
  mergeServerList(acc, neo);

  return acc;
};

export const mergeFilteredMapLinkData = (
  current: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.LinkData;
  },
  newNode: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.LinkData;
  },
) => {
  const acc = current?.data;
  const neo = newNode?.data;

  acc.hasAlert = neo.hasAlert;
  acc.slowCount += neo.slowCount;
  acc.errorCount += neo.errorCount;
  acc.totalCount += neo.totalCount;

  // mergeLinkAgentIdNameMap(acc, neo, 'fromAgentIdNameMap');
  // mergeLinkAgentIdNameMap(acc, neo, 'toAgentIdNameMap');
  mergeHistogram(acc, neo);
  mergeLinkAgentIds(acc, neo, 'fromAgents');
  mergeLinkAgentIds(acc, neo, 'toAgents');
  mergeResponseStatistics(acc, neo);
  mergeTimeSeriesHistogram(current, newNode);
  mergeAgentTimeSeriesHistogramByType(
    {
      timestamp: current?.timestamp,
      data: acc.sourceTimeSeriesHistogram,
    },
    {
      timestamp: newNode?.timestamp,
      data: neo.sourceTimeSeriesHistogram,
    },
  );
  mergeHistogramByType(acc, neo, 'sourceHistogram');
  mergeHistogramByType(acc, neo, 'targetHistogram');
  mergeResponseStatisticsByType(acc, neo, 'sourceResponseStatistics');
  mergeResponseStatisticsByType(acc, neo, 'targetResponseStatistics');

  return acc;
};

function mergeAgentIds(old: FilteredMap.NodeData, neo: FilteredMap.NodeData): void {
  neo.agents.forEach((agent: FilteredMap.Agent) => {
    const oldAgentIndex = old.agents.findIndex((oldAgent) => oldAgent.id === agent.id);
    if (oldAgentIndex === -1) {
      old.agents.push(agent);
    }
  });
}

function mergeLinkAgentIds(
  old: FilteredMap.LinkData,
  neo: FilteredMap.LinkData,
  type: 'fromAgents' | 'toAgents',
): void {
  const oldAgents = old[type];
  const newAgents = neo[type];
  if (newAgents) {
    newAgents?.forEach((agent: FilteredMap.Agent) => {
      const oldAgentIndex = oldAgents?.findIndex((oldAgent) => oldAgent.id === agent.id);
      if (oldAgentIndex === -1) {
        old[type]?.push(agent);
      }
    });
  }
}

function mergeHistogram(
  old: FilteredMap.NodeData | FilteredMap.LinkData,
  neo: FilteredMap.NodeData | FilteredMap.LinkData,
): void {
  if (neo.histogram) {
    if (old.histogram) {
      mergeHistogramType(old.histogram, neo.histogram);
    } else {
      old.histogram = neo.histogram;
    }
  }
}

function mergeResponseStatistics(
  old: FilteredMap.NodeData | FilteredMap.LinkData,
  neo: FilteredMap.NodeData | FilteredMap.LinkData,
): void {
  if (neo.responseStatistics) {
    if (old.responseStatistics) {
      mergeResponseStatisticsType(old.responseStatistics, neo.responseStatistics);
    } else {
      old.responseStatistics = neo.responseStatistics;
    }
  }
}

function mergeResponseStatisticsType(
  old: FilteredMap.ResponseStatistics,
  neo: FilteredMap.ResponseStatistics,
): void {
  if (neo) {
    (Object.keys(neo) as Array<keyof FilteredMap.ResponseStatistics>).forEach((key) => {
      if (key === 'Max') {
        old.Max = Math.max(old.Max, neo.Max);
        return;
      }
      if (key === 'Avg') {
        return;
      }
      old[key] += neo[key];
    });
    old.Avg = old.Tot > 0 ? Math.floor(old.Sum / old.Tot) : 0;
  }
}

// function mergeHistogramType(oldHistogram: FilteredMap.Histogram | IResponseMilliSecondTime, neoHistogram: FilteredMap.Histogram | IResponseMilliSecondTime): void {
function mergeHistogramType(
  oldHistogram: FilteredMap.Histogram,
  neoHistogram: FilteredMap.Histogram,
): void {
  if (neoHistogram) {
    Object.keys(neoHistogram).forEach((k: string) => {
      const key = k as keyof FilteredMap.Histogram;
      oldHistogram[key] += neoHistogram[key];
    });
  }
}

function mergeAgentHistogram(old: FilteredMap.NodeData, neo: FilteredMap.NodeData): void {
  Object.keys(neo.agentHistogram).forEach((key: string) => {
    if (old.agentHistogram[key]) {
      mergeHistogramType(old.agentHistogram[key], neo.agentHistogram[key]);
    } else {
      old.agentHistogram[key] = neo.agentHistogram[key];
    }
  });
}

// 내부 값의 순서가 보장되어야한 유의미한 코드
function mergeTimeSeriesHistogram(
  old: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.NodeData | FilteredMap.LinkData;
  },
  neo: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.NodeData | FilteredMap.LinkData;
  },
): void {
  if (neo.data?.timeSeriesHistogram) {
    if (old.data?.timeSeriesHistogram) {
      neo?.data?.timeSeriesHistogram?.forEach((obj, outerIndex: number) => {
        if (obj.key === 'Avg') {
          return;
        }
        if (obj.key === 'Max') {
          old?.data?.timeSeriesHistogram?.[outerIndex]?.values?.forEach((value, valueIndex) => {
            const oldTimestamp = old?.timestamp?.[valueIndex];
            const newTimestampIndex = neo?.timestamp?.findIndex(
              (timestamp) => timestamp === oldTimestamp,
            );
            if (newTimestampIndex !== -1) {
              old.data.timeSeriesHistogram![outerIndex].values![valueIndex] = Math.max(
                value,
                obj.values![newTimestampIndex] || 0,
              );
            }
          });
          return;
        }
        old.data?.timeSeriesHistogram?.[outerIndex]?.values?.forEach((value, valueIndex) => {
          const oldTimestamp = old?.timestamp?.[valueIndex];
          const newTimestampIndex = neo?.timestamp?.findIndex(
            (timestamp) => timestamp === oldTimestamp,
          );

          if (newTimestampIndex !== -1) {
            old.data.timeSeriesHistogram![outerIndex].values![valueIndex] +=
              obj.values![newTimestampIndex] || 0;
          }
        });
      });
      updateAvgTimeSeriesHistogram(old?.data?.timeSeriesHistogram, old?.timestamp);
    } else {
      old.data.timeSeriesHistogram = neo?.data?.timeSeriesHistogram;
    }
  }
}

function updateAvgTimeSeriesHistogram(
  histArray: FilteredMap.TimeSeriesHistogram[],
  timestamp: FilteredMap.ApplicationMapData['timestamp'],
): void {
  const mapSum = {} as { [key: number]: number };
  const mapTot = {} as { [key: number]: number };
  let avgHistogram: FilteredMap.TimeSeriesHistogram | undefined;
  let avgHistogramIndex = -1;
  histArray.forEach((histogram: FilteredMap.TimeSeriesHistogram, outerIndex: number) => {
    if (histogram.key === 'Avg') {
      avgHistogram = histogram;
      avgHistogramIndex = outerIndex;
    }
    if (histogram.key === 'Tot') {
      histogram.values.forEach((chartValue, valueIndex) => {
        mapTot[timestamp[valueIndex]] = chartValue;
      });
    }
    if (histogram.key === 'Sum') {
      histogram.values.forEach((chartValue, valueIndex) => {
        mapSum[timestamp[valueIndex]] = chartValue;
      });
    }
  });
  if (avgHistogram) {
    avgHistogram.values.forEach((info: number, valueIndex) => {
      const timestampV = timestamp[valueIndex];
      avgHistogram!.values[valueIndex] =
        mapTot[timestampV] > 0 ? Math.floor(mapSum[timestampV] / mapTot[timestampV]) : 0;
    });
    if (avgHistogramIndex >= 0) {
      histArray[avgHistogramIndex] = avgHistogram;
    }
  }
}

function mergeAgentTimeSeriesHistogramByType(
  current: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.SourceTimeSeriesHistogram | FilteredMap.AgentTimeSeriesHistogram;
  },
  newNode: {
    timestamp: FilteredMap.ApplicationMapData['timestamp'];
    data: FilteredMap.SourceTimeSeriesHistogram | FilteredMap.AgentTimeSeriesHistogram;
  },
): void {
  let old = current.data;
  const neo = newNode.data;

  if (neo) {
    if (old) {
      Object.keys(neo).forEach((agentId: string) => {
        if (old[agentId]) {
          neo[agentId].forEach((obj, outerIndex) => {
            if (obj.key === 'Avg') {
              return;
            }
            if (obj.key === 'Max') {
              old[agentId][outerIndex].values.forEach((value, valueIndex) => {
                const oldTimestamp = old?.timestamp?.[valueIndex];
                const newTimestampIndex = neo?.timestamp?.findIndex(
                  (timestamp) => timestamp === oldTimestamp,
                );

                if (newTimestampIndex !== -1) {
                  old[agentId]![outerIndex].values![valueIndex] = Math.max(
                    value,
                    obj.values![newTimestampIndex] || 0,
                  );
                }
              });

              return;
            }
            old[agentId][outerIndex].values.forEach((value, valueIndex) => {
              const oldTimestamp = old?.timestamp?.[valueIndex];
              const newTimestampIndex = neo?.timestamp?.findIndex(
                (timestamp) => timestamp === oldTimestamp,
              );

              if (newTimestampIndex !== -1) {
                old[agentId]![outerIndex].values![valueIndex] +=
                  obj.values![newTimestampIndex] || 0;
              }
            });
          });
          updateAvgTimeSeriesHistogram(old[agentId], current.timestamp);
        } else {
          old[agentId] = neo[agentId];
        }
      });
    } else {
      old = neo;
    }
  }
}

function mergeServerList(old: FilteredMap.NodeData, neo: FilteredMap.NodeData): void {
  if (neo.serverList) {
    if (old.serverList) {
      Object.keys(neo.serverList).forEach((key: string) => {
        if (old.serverList[key]) {
          Object.keys(neo.serverList[key].instanceList).forEach((instanceKey: string) => {
            if (!(instanceKey in old.serverList[key].instanceList)) {
              old.serverList[key].instanceList[instanceKey] =
                neo.serverList[key].instanceList[instanceKey];
            }
          });
        } else {
          old.serverList[key] = neo.serverList[key];
        }
      });
    } else {
      old.serverList = neo.serverList;
    }
  }
}

function mergeHistogramByType(
  old: FilteredMap.LinkData,
  neo: FilteredMap.LinkData,
  histogramType: 'sourceHistogram' | 'targetHistogram',
): void {
  if (!neo[histogramType]) {
    return;
  }
  if (old[histogramType]) {
    Object.keys(neo[histogramType]).forEach((key: string) => {
      if (old[histogramType][key]) {
        Object.keys(neo[histogramType][key]).forEach((k: string) => {
          const histogramKey = k as keyof FilteredMap.Histogram;
          old[histogramType][key][histogramKey] += neo[histogramType][key][histogramKey];
        });
      } else {
        old[histogramType][key] = neo[histogramType][key];
      }
    });
  }
}

function mergeResponseStatisticsByType(
  old: FilteredMap.LinkData,
  neo: FilteredMap.LinkData,
  srcOrTarget: 'sourceResponseStatistics' | 'targetResponseStatistics',
): void {
  if (!neo[srcOrTarget]) {
    return;
  }
  if (old[srcOrTarget]) {
    const oldTarget = old[srcOrTarget];
    const neoTarget = neo[srcOrTarget];
    Object.keys(neoTarget).forEach((agentId: string) => {
      if (oldTarget[agentId]) {
        Object.keys(neoTarget[agentId]).forEach((t: string) => {
          const type = t as keyof FilteredMap.ResponseStatistics;
          if (type === 'Max') {
            oldTarget[agentId][type] = Math.max(oldTarget[agentId][type], neoTarget[agentId][type]);
            return;
          }
          if (type === 'Avg') {
            return;
          }
          oldTarget[agentId][type] += neoTarget[agentId][type];
        });
        oldTarget[agentId]['Avg'] =
          oldTarget[agentId]['Tot'] > 0
            ? Math.floor(oldTarget[agentId]['Sum'] / oldTarget[agentId]['Tot'])
            : 0;
      } else {
        oldTarget[agentId] = neoTarget[agentId];
      }
    });
  }
}
