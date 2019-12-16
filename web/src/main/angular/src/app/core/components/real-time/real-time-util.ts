import { IActiveThreadCounts, ResponseCode } from './real-time-websocket.service';
import { filterObj } from 'app/core/utils/util';
import { IParsedATC, ChartState } from './real-time-chart.component';

export function getSuccessDataList(atc: { [key: string]: IActiveThreadCounts }): number[][] {
    const successATC = filterObj((agentName: string) => atc[agentName].code === ResponseCode.SUCCESS, atc);

    return Object.keys(successATC).map((agentName: string) => atc[agentName].status);
}

export function getTotalResponseCount(dataList: number[][]): number[] {
    return dataList.reduce((acc: number[], curr: number[]) => {
        return acc.map((a: number, i: number) => a + curr[i]);
    }, [0, 0, 0, 0]);
}

export function getATCforAgent(prevATC: { [key: string]: IActiveThreadCounts }, currATC: { [key: string]: IActiveThreadCounts }): { [key: string]: IParsedATC } {
    const isFirstData = !prevATC;
    const prevATCKeys = isFirstData ? [] : Object.keys(prevATC);
    const currATCKeys = Object.keys(currATC);
    const mergedKeys = [...new Set([...prevATCKeys, ...currATCKeys])];
    const atc = mergedKeys.reduce((acc: { [key: string]: IParsedATC }, key: string) => {
        return !prevATCKeys.includes(key) ? { ...acc, [key]: { ...currATC[key], chartState: isFirstData ? ChartState.NORMAL : ChartState.ADDED } }
            : !currATCKeys.includes(key) ? { ...acc, [key]: { ...prevATC[key], chartState: ChartState.REMOVED }}
            : { ...acc, [key]: { ...currATC[key], chartState: ChartState.NORMAL }};
    }, {});

    return atc;
}

export function getATCforTotal(applicationName: string, atc: { [key: string]: IActiveThreadCounts }): { [key: string]: IParsedATC } {
    const successDataList = getSuccessDataList(atc);
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

export function getFilteredATC(atc: { [key: string]: IParsedATC }): { [key: string]: IParsedATC } {
    return filterObj((agentName: string) => atc[agentName].code === ResponseCode.SUCCESS, atc);
}
