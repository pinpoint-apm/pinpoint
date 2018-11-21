import { IActiveThreadCounts, ResponseCode } from 'app/core/components/real-time-new/new-real-time-websocket.service';

export function getSuccessDataList(obj: { [key: string]: IActiveThreadCounts }): number[][] {
    return Object.keys(obj)
        .filter((agentName: string) => obj[agentName].code === ResponseCode.SUCCESS)
        .map((agentName: string) => obj[agentName].status);
}

export function getTotalResponseCount(dataList: number[][]): number[] {
    return dataList.reduce((acc: number[], curr: number[]) => {
        return acc.map((a: number, i: number) => a + curr[i]);
    }, [0, 0, 0, 0]);
}
