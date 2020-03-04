export class MergeServerMapData {
    static mergeNodeData(currentNodeData: INodeInfo, newNodeData: INodeInfo): void {
        const old = currentNodeData;
        const neo = newNodeData;

        old.hasAlert = neo.hasAlert;
        old.slowCount += neo.slowCount;
        old.errorCount += neo.errorCount;
        old.totalCount += neo.totalCount;
        old.instanceCount = Math.max(old.instanceCount, neo.instanceCount);
        old.instanceErrorCount = Math.max(old.instanceErrorCount, neo.instanceErrorCount);

        mergeAgentIds(old, neo);
        mergeHistogram(old, neo);
        mergeAgentHistogram(old, neo);
        mergeTimeSeriesHistogram(old, neo);
        mergeAgentTimeSeriesHistogramByType(old, neo, 'agentTimeSeriesHistogram');
        mergeServerList(old, neo);
    }
    static mergeLinkData(currentLinkData: ILinkInfo, newLinkData: ILinkInfo): void {
        const old = currentLinkData;
        const neo = newLinkData;

        old.hasAlert = neo.hasAlert;
        old.slowCount += neo.slowCount;
        old.errorCount += neo.errorCount;
        old.totalCount += neo.totalCount;

        mergeHistogram(old, neo);
        mergeTimeSeriesHistogram(old, neo);
        mergeAgentTimeSeriesHistogramByType(old, neo, 'sourceTimeSeriesHistogram');
        mergeHistogramByType(old, neo, 'sourceHistogram');
        mergeHistogramByType(old, neo, 'targetHistogram');
    }
}
function mergeAgentIds(old: INodeInfo, neo: INodeInfo): void {
    neo.agentIds.forEach((agentId: string) => {
        if (old.agentIds.indexOf(agentId) === -1) {
            old.agentIds.push(agentId);
        }
    });
}
function mergeHistogram(old: INodeInfo | ILinkInfo, neo: INodeInfo | ILinkInfo): void {
    if (neo.histogram) {
        if (old.histogram) {
            mergeHistogramType(old.histogram, neo.histogram);
        } else {
            old.histogram = neo.histogram;
        }
    }
}
function mergeHistogramType(oldHistogram: IResponseTime | IResponseMilliSecondTime, neoHistogram: IResponseTime | IResponseMilliSecondTime): void {
    if (neoHistogram) {
        Object.keys(neoHistogram).forEach((key: string) => {
            oldHistogram[key] += neoHistogram[key];
        });
    }
}
function mergeAgentHistogram(old: INodeInfo, neo: INodeInfo): void {
    Object.keys(neo.agentHistogram).forEach((key: string) => {
        if (old.agentHistogram[key]) {
            mergeHistogramType(old.agentHistogram[key], neo.agentHistogram[key]);
        } else {
            old.agentHistogram[key] = neo.agentHistogram[key];
        }
    });
}
// 내부 값의 순서가 보장되어야한 유의미한 코드
function mergeTimeSeriesHistogram(old: INodeInfo | ILinkInfo, neo: INodeInfo | ILinkInfo): void {
    if (neo.timeSeriesHistogram) {
        if (old.timeSeriesHistogram) {
            neo.timeSeriesHistogram.forEach((obj: any, outerIndex: number) => {
                obj.values.forEach((chartValue: any, innerIndex: number) => {
                    old.timeSeriesHistogram[outerIndex].values[innerIndex][1] += chartValue[1];
                });
            });
        } else {
            old.timeSeriesHistogram = neo.timeSeriesHistogram;
        }
    }
}
function mergeAgentTimeSeriesHistogramByType(old: INodeInfo | ILinkInfo, neo: INodeInfo | ILinkInfo, type: string): void {
    if (neo[type]) {
        if (old[type]) {
            Object.keys(neo[type]).forEach((agentId: string) => {
                if (old[type][agentId]) {
                    neo[type][agentId].forEach((obj: any, outerIndex: number) => {
                        obj.values.forEach((chartValue: any, innerIndex: number) => {
                            old[type][agentId][outerIndex].values[innerIndex][1] += chartValue[1];
                        });
                    });
                } else {
                    old[type][agentId] = neo[type][agentId];
                }
            });
        } else {
            old[type] = neo[type];
        }
    }
}
function mergeServerList(old: INodeInfo, neo: INodeInfo): void {
    if (neo.serverList) {
        if (old.serverList) {
            Object.keys(neo.serverList).forEach((key: string) => {
                if (old.serverList[key]) {
                    Object.keys(neo.serverList[key].instanceList).forEach((instanceKey: string) => {
                        if ((instanceKey in old.serverList[key].instanceList) === false) {
                            old.serverList[key].instanceList[instanceKey] = neo.serverList[key].instanceList[instanceKey];
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
function mergeHistogramByType(old: ILinkInfo, neo: ILinkInfo, histogramType: string): void {
    if (old[histogramType]) {
        Object.keys(neo[histogramType]).forEach((key: string) => {
            if (old[histogramType][key]) {
                Object.keys(neo[histogramType][key]).forEach((histogramKey: string) => {
                    old[histogramType][key][histogramKey] += neo[histogramType][key][histogramKey];
                });
            } else {
                old[histogramType][key] = neo[histogramType][key];
            }
        });
    } else {
        old[histogramType] = neo[histogramType];
    }
}
