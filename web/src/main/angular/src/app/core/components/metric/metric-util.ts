export const enum Unit {
    Byte = 'byte',
    Count = 'count',
    Percent = 'percent'
}

export function getMetaInfo(dataUnit: Unit): {yMax: number, getFormat: Function} {
    return {
        // yMax: dataUnit === Unit.Percent ? 100 : undefined,
        // * Set yMax as undefined for every case temporarily
        yMax: undefined,
        getFormat: (value: number) => dataUnit === Unit.Percent ? Number.isInteger(value) ? `${value}%` : `${value.toFixed(2)}%`
            : dataUnit === Unit.Count ? value.toLocaleString()
            : convertWithUnit(value)
    };
}

function convertWithUnit(value: number): string {
    const unitList = ['', 'K', 'M', 'G'];

    return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
        const v = Number(acc);

        return v >= 1000
            ? (v / 1000).toString()
            : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
    }, value.toString());
}
