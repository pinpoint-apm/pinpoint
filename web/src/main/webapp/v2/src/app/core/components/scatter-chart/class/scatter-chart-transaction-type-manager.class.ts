import { ITypeInfo } from './scatter-chart.class';

export class ScatterChartTransactionTypeManager {
    private dataByIndex: { [key: number]: ITypeInfo } = {};
    private dataByName: { [key: string]: ITypeInfo } = {};

    constructor(typeInfos: ITypeInfo[]) {
        this.initData(typeInfos);
    }
    private initData(typeInfos: ITypeInfo[]): void {
        typeInfos.forEach((typeInfo: ITypeInfo, index: number) => {
            const newTypeInfo = {
                name: typeInfo.name,
                color: typeInfo.color,
                order: typeInfo.order,
                index: index,
                checked: true
            };
            this.dataByName[typeInfo.name] = newTypeInfo;
            this.dataByIndex[index] = newTypeInfo;
        });
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
        const list = [];
        Object.keys(this.dataByName).forEach((name: string) => {
            if (this.dataByName[name].checked) {
                list.push(name);
            }
        });
        return list;
    }
}
