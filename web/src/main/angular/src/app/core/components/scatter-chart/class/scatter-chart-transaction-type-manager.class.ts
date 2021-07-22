export interface ITransactionTypeInfo {
    name: string;
    order: number;
    color: string;
    dataIndex: number;
    checked: boolean;
    value: number;
}

export class ScatterChartTransactionTypeManager {
    private dataByIndex: { [key: number]: ITransactionTypeInfo } = {};
    private dataByName: { [key: string]: ITransactionTypeInfo } = {};

    computedStyle = getComputedStyle(document.body);
    chartColor = {
        success: this.computedStyle.getPropertyValue('--chart-success'),
        fail: this.computedStyle.getPropertyValue('--chart-fail'),
    };

    static getTypeCheckValue(onlyFailed: boolean, onlySuccess: boolean): any {
        if (onlyFailed) {
            return {
                name: 'success',
                checked: false
            };
        } else if (onlySuccess) {
            return {
                name: 'failed',
                checked: false
            };
        }
    }
    static getDefaultTransactionTypeInfo(): {[key: string]: ITransactionTypeInfo} {
        const computedStyle = getComputedStyle(document.body);
        const chartColor = {
            success: computedStyle.getPropertyValue('--chart-success'),
            fail: computedStyle.getPropertyValue('--chart-fail'),
        };
        return {
            success: {
                name: 'success',
                order: 10,
                color: chartColor.success,
                checked: true,
                dataIndex: 1,
                value: 0
            },
            failed: {
                name: 'failed',
                order: 20,
                color: chartColor.fail,
                checked: true,
                dataIndex: 0,
                value: 0
            }
        };
    }
    constructor(private transactionTypeInfo: {[key: string]: ITransactionTypeInfo}) {
        this.initData();
    }
    private initData(): void {
        Object.keys(this.transactionTypeInfo).forEach((key: string) => {
            const obj = {
                ...this.transactionTypeInfo[key]
            };
            this.dataByName[key] = obj;
            this.dataByIndex[obj.dataIndex] = obj;
        });
    }
    reset(): void {
        this.initData();
    }
    getTypeNameList(): string[] {
        return Object.keys(this.dataByName);
    }
    setChecked(name: string, checked: boolean): void {
        this.dataByName[name].checked = checked;
    }
    isCheckedByName(name: string): boolean {
        return this.dataByName[name].checked;
    }
    isCheckedByIndex(index: number): boolean {
        return this.dataByIndex[index].checked;
    }
    getColorByIndex(index: number): string {
        return this.dataByIndex[index].color;
    }
    getColorByName(name: string): string {
        return this.dataByName[name].color;
    }
    getNameByIndex(index: number): string {
        return this.dataByIndex[index].name;
    }
    getCheckedTypeNameList(): string[] {
        const list: string[] = [];
        Object.keys(this.dataByName).forEach((name: string) => {
            if (this.dataByName[name].checked) {
                list.push(name);
            }
        });
        return list;
    }
}
