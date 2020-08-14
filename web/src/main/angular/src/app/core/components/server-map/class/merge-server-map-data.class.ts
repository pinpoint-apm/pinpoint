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
        old.maxElapsed = Math.max(old.maxElapsed, neo.maxElapsed);
        old.sumElapsed += neo.sumElapsed;
        old.avgElapsed = old.totalCount > 0 ? Math.floor(old.sumElapsed / old.totalCount) : 0;

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
        old.maxElapsed = Math.max(old.maxElapsed, neo.maxElapsed);
        old.sumElapsed += neo.sumElapsed;
        old.avgElapsed = old.totalCount > 0 ? Math.floor(old.sumElapsed / old.totalCount) : 0;

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
        old.histogram.Avg = old.avgElapsed;
    }
}
function mergeHistogramType(oldHistogram: IResponseTime | IResponseMilliSecondTime, neoHistogram: IResponseTime | IResponseMilliSecondTime): void {
    if (neoHistogram) {
        Object.keys(neoHistogram).forEach((key: string) => {
            if (key === "Max") {
                oldHistogram[key] = Math.max(oldHistogram[key], neoHistogram[key]);
                return;
            }
            if (key === "Avg") {
                return;
            }
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
        old.agentHistogram[key].Avg = old.agentHistogram[key].Tot > 0 ?
            Math.floor(old.agentHistogram[key].Sum / old.agentHistogram[key].Tot) : 0;
    });
}
// 내부 값의 순서가 보장되어야한 유의미한 코드
function mergeTimeSeriesHistogram(old: INodeInfo | ILinkInfo, neo: INodeInfo | ILinkInfo): void {
    if (neo.timeSeriesHistogram) {
        if (old.timeSeriesHistogram) {
            neo.timeSeriesHistogram.forEach((obj: any, outerIndex: number) => {
                if (obj.key === "Avg") {
                    return;
                }
                if (obj.key === "Max") {
                    obj.values.forEach((chartValue: any, innerIndex: number) => {
                        old.timeSeriesHistogram[outerIndex].values[innerIndex][1] =
                            Math.max(old.timeSeriesHistogram[outerIndex].values[innerIndex][1], chartValue[1]);
                    });
                    return;
                }
                obj.values.forEach((chartValue: any, innerIndex: number) => {
                    old.timeSeriesHistogram[outerIndex].values[innerIndex][1] += chartValue[1];
                });
            });
            updateAvgTimeSeriesHistogram(old.timeSeriesHistogram);
        } else {
            old.timeSeriesHistogram = neo.timeSeriesHistogram;
        }
    }
}
function updateAvgTimeSeriesHistogram(histArray: IHistogram[]) {
    const mapSum = {};
    const mapTot = {};
    let avgHistogram:IHistogram;
    let avgHistogramIndex: number = -1;
    histArray.forEach((histogram:IHistogram, outerIndex:number) => {
        if (histogram.key === "Avg") {
            avgHistogram = histogram;
            avgHistogramIndex = outerIndex;
        }
        if (histogram.key === "Tot") {
            histogram.values.forEach((chartValue: any) => {
                mapTot[chartValue[0]] = chartValue[1];
            });
        }
        if (histogram.key === "Sum") {
            histogram.values.forEach((chartValue: any) => {
                mapSum[chartValue[0]] = chartValue[1];
            });
        }
    });
    if (avgHistogram) {
        avgHistogram.values.forEach((info:number[]) => {
            const timestamp = info[0];
            info[1] = mapTot[timestamp] > 0 ? Math.floor(mapSum[timestamp] / mapTot[timestamp]) : 0;
        });
        if (avgHistogramIndex >= 0) {
            histArray[avgHistogramIndex] = avgHistogram;
        }
    }
}
function mergeAgentTimeSeriesHistogramByType(old: INodeInfo | ILinkInfo, neo: INodeInfo | ILinkInfo, type: string): void {
    if (neo[type]) {
        if (old[type]) {
            Object.keys(neo[type]).forEach((agentId: string) => {
                if (old[type][agentId]) {
                    neo[type][agentId].forEach((obj: any, outerIndex: number) => {
                        if (obj.key === "Avg") {
                            return;
                        }
                        if (obj.key === "Max") {
                            obj.values.forEach((chartValue: any, innerIndex: number) => {
                                old[type][agentId][outerIndex].values[innerIndex][1] =
                                    Math.max(old[type][agentId][outerIndex].values[innerIndex][1], chartValue[1]);
                            });
                            return;
                        }
                        obj.values.forEach((chartValue: any, innerIndex: number) => {
                            old[type][agentId][outerIndex].values[innerIndex][1] += chartValue[1];
                        });
                    });
                    updateAvgTimeSeriesHistogram(old[type][agentId]);
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
                    if (histogramKey === "Max") {
                        old[histogramType][key][histogramKey] = Math.max(old[histogramType][key][histogramKey], neo[histogramType][key][histogramKey]);
                        return;
                    }
                    if (histogramKey === "Avg") {
                        return;
                    }
                    old[histogramType][key][histogramKey] += neo[histogramType][key][histogramKey];
                });
                old[histogramType][key]["Avg"] = old[histogramType][key]["Tot"] > 0 ?
                    Math.floor(old[histogramType][key]["Sum"] / old[histogramType][key]["Tot"]) : 0;
            } else {
                old[histogramType][key] = neo[histogramType][key];
            }
        });
    } else {
        old[histogramType] = neo[histogramType];
    }
}
