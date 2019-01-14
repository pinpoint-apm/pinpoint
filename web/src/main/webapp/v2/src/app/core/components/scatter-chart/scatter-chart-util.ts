export default {
    // compare(aData: any[][], dataIndex: number, fnCompare: (result: number) => boolean): any {
    //     let targetIndex = 0;
    //     for (let i = 1, len = aData.length; i < len; i++) {
    //         const result = aData[targetIndex][dataIndex] - aData[i][dataIndex];
    //         if (fnCompare(result)) {
    //             targetIndex = i;
    //         }
    //     }
    //     return aData[targetIndex][dataIndex];
    // },
    // min(result: number): boolean {
    //     return result > 0;
    // },
    // max(result: number): boolean {
    //     return result < 0;
    // },
    // indexOf(aData: any[], value: any): number {
    //     for (let i = 0 ; i < aData.length ; i++) {
    //         if (aData[i] === value) {
    //             return i;
    //         }
    //     }
    //     return -1;
    // },
    // getBoundaryValue(oRange: { min: number, max: number}, value: number): number {
    //     return Math.min(oRange.max, Math.max(oRange.min, value));
    // },
    // isInRange(from: number, to: number, value: number): boolean {
    //     return value >= from && value <= to;
    // },
    // isEmpty(obj: any): boolean {
    //     let count = 0;
    //     Object.keys(obj).forEach((key: string) => {
    //         count += obj[key].length;
    //     });
    //     return count === 0;
    // },
    // makeKey(a: any, b: any, c: any): string {
    //     return a + '-' + b + '-' + c;
    // },
    // isString(v: any): boolean {
    //     return typeof v === 'string';
    // },
};
