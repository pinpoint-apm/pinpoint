export function makeXData(data: number[]): number[] {
    return data;
}

export function makeYData(data: (number | string)[][], dataIndex: number): number[] | string[] {
    return data.map((d: (number | string)[]) => d[dataIndex] as any);
}

export function getMaxTickValue(v: number): number {
    return Math.ceil(v / 4) * 4;
}
