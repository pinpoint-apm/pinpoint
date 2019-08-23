export interface ITransactionTypeInfo {
    name: string;
    order: number;
    color: string;
    dataIndex: number;
    checked: boolean;
}

export class ScatterChartTransactionTypeManager {
    private dataByIndex: { [key: number]: ITransactionTypeInfo } = {};
    private dataByName: { [key: string]: ITransactionTypeInfo } = {};

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
        return {
            success: {
                name: 'success',
                order: 10,
                color: '#34B994',
                checked: true,
                dataIndex: 1
            },
            failed: {
                name: 'failed',
                order: 20,
                color: '#E95459',
                checked: true,
                dataIndex: 0
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
