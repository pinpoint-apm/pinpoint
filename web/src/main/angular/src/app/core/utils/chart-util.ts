import { PrimitiveArray } from 'billboard.js';

export function makeXData(data: number[]): number[] {
    return data;
}

export function makeYData(data: (number | string)[][], dataIndex: number): number[] | string[] {
    return data.map((d: (number | string)[]) => {
        const value = d[dataIndex];

        return (typeof value === 'string' || value >= 0 ? value : null) as any;
    });
}

export function getMaxTickValue(data: PrimitiveArray[], startIndex: number, endIndex?: number): number {
    const maxData = Math.max(...data.slice(startIndex, endIndex).map((d: PrimitiveArray) => d.slice(1)).flat() as number[]);
    const adjustedMax = maxData + maxData / 4;
    const baseUnitNumber = 1000;
    const maxTick = Array(4).fill(0).reduce((acc: number, _: number, i: number, arr: number[]) => {
        const unitNumber = Math.pow(baseUnitNumber, i);

        return acc / unitNumber >= baseUnitNumber
            ? acc
            : (arr.splice(i + 1), getNearestNiceNumber(acc / unitNumber) * unitNumber);
    }, adjustedMax);

    return maxTick;
}

export function getStackedData(data: PrimitiveArray[]): PrimitiveArray[] {
    return [
        data[0],
        ['rs', ...data.slice(1).map((d: PrimitiveArray) => d.slice(1)).reduce((acc: number[], curr: number[]) => {
            return acc.map((v: number, i: number) => v + curr[i]);
        }, Array(data[0].length - 1).fill(0))]
    ];
}

function getNearestNiceNumber(v: number): number {
    /**
     * ex: v = 10.2345
     * 정수부 자릿수 switch/case
     * 1자리면 4의배수
     * 2자리면 20의배수
     * 3자리면 100의배수
     * v보다 큰 가장가까운 수 리턴해주기.
     */
    const integerPartLength = v.toString().split('.')[0].length;

    switch (integerPartLength) {
        case 1:
            return Math.ceil(v / 4) * 4;
        case 2:
            return Math.ceil(v / 20) * 20;
        case 3:
            return Math.ceil(v / 100) * 100;
    }
}
