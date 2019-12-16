import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter } from '@angular/core';
import { ScatterChartTransactionTypeManager, ITransactionTypeInfo } from './class/scatter-chart-transaction-type-manager.class';
@Component({
    selector: 'pp-scatter-chart-state-view',
    templateUrl: './scatter-chart-state-view.component.html',
    styleUrls: ['./scatter-chart-state-view.component.css']
})
export class ScatterChartStateViewComponent implements OnInit, OnChanges {
    @Input() instanceKey: string;
    @Input() transactionTypeCount: any;
    @Output() outChanged: EventEmitter<{instanceKey: string, name: string, checked: boolean}> = new EventEmitter();
    sortedKeyArr: string[];
    transactionTypeInfo: {[key: string]: ITransactionTypeInfo};
    constructor() {}
    ngOnInit() {
        this.transactionTypeInfo = ScatterChartTransactionTypeManager.getDefaultTransactionTypeInfo();
        this.sortTypeInfo();
    }
    ngOnChanges(simpleChanges: SimpleChanges) {
        if (this.transactionTypeCount && this.transactionTypeInfo) {
            Object.keys(this.transactionTypeCount).forEach((key: string) => {
                this.transactionTypeInfo[key].checked = this.transactionTypeCount[key].checked;
            });
        }
    }
    private sortTypeInfo(): void {
        this.sortedKeyArr = [];
        const temp: ITransactionTypeInfo[] = [];
        Object.keys(this.transactionTypeInfo).forEach((key: string) => {
            temp.push(this.transactionTypeInfo[key]);
        });
        temp.sort((a: ITransactionTypeInfo, b: ITransactionTypeInfo): number => {
            return a.order - b.order;
        }).forEach((value: ITransactionTypeInfo) => {
            this.sortedKeyArr.push(value.name);
        });
    }
    upperFirstChar(str: string): string {
        return str ? str.substr(0, 1).toUpperCase() + str.substr(1) : '';
    }
    getTypeCount(name: string): number {
        if (this.transactionTypeCount && this.transactionTypeCount[name]) {
            return this.transactionTypeCount[name].count;
        } else {
            return 0;
        }
    }
    onCheck(typeName: string): void {
        this.transactionTypeInfo[typeName].checked = !this.transactionTypeInfo[typeName].checked;
        this.outChanged.emit({
            instanceKey: this.instanceKey,
            name: typeName,
            checked: this.transactionTypeInfo[typeName].checked
        });
    }
}
