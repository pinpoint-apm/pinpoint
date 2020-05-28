import { IActiveRequestCounts, ResponseCode } from './real-time-websocket.service';
import { filterObj } from 'app/core/utils/util';
import { IParsedARC, ChartState } from './real-time-chart.component';

export function getSuccessDataList(arc: {[key: string]: IActiveRequestCounts}): number[][] {
    const successARC = filterObj((agentName: string) => arc[agentName].code === ResponseCode.SUCCESS, arc);

    return Object.keys(successARC).map((agentName: string) => arc[agentName].status);
}

export function getTotalResponseCount(dataList: number[][]): number[] {
    return dataList.reduce((acc: number[], curr: number[]) => {
        return acc.map((a: number, i: number) => a + curr[i]);
    }, [0, 0, 0, 0]);
}

export function getARCforAgent(prevARC: {[key: string]: IActiveRequestCounts}, currARC: {[key: string]: IActiveRequestCounts}): {[key: string]: IParsedARC} {
    const isFirstData = !prevARC;
    const prevARCKeys = isFirstData ? [] : Object.keys(prevARC);
    const currARCKeys = Object.keys(currARC);
    const mergedKeys = [...new Set([...prevARCKeys, ...currARCKeys])];
    const arc = mergedKeys.reduce((acc: {[key: string]: IParsedARC}, key: string) => {
        return !prevARCKeys.includes(key) ? {...acc, [key]: {...currARC[key], chartState: isFirstData ? ChartState.NORMAL : ChartState.ADDED}}
            : !currARCKeys.includes(key) ? {...acc, [key]: {...prevARC[key], chartState: ChartState.REMOVED}}
            : { ...acc, [key]: { ...currARC[key], chartState: ChartState.NORMAL }};
    }, {});

    return arc;
}

export function getARCforTotal(applicationName: string, arc: {[key: string]: IActiveRequestCounts}): {[key: string]: IParsedARC} {
    const successDataList = getSuccessDataList(arc);
    const hasError = successDataList.length === 0;
    const totalResponseCount = getTotalResponseCount(successDataList);

    return {
        [applicationName]: {
            code: hasError ? ResponseCode.ERROR_BLACK : ResponseCode.SUCCESS,
            message: hasError ? 'ERROR' : 'OK',
            status: hasError ? [] : totalResponseCount,
            chartState: ChartState.NORMAL
        }
    };
}

export function getFilteredARC(arc: {[key: string]: IParsedARC}): {[key: string]: IParsedARC} {
    return filterObj((agentName: string) => arc[agentName].code === ResponseCode.SUCCESS, arc);
}
