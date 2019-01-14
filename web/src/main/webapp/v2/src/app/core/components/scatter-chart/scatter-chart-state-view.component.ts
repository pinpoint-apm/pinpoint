import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-scatter-chart-state-view',
    templateUrl: './scatter-chart-state-view.component.html',
    styleUrls: ['./scatter-chart-state-view.component.css']
})
export class ScatterChartStateViewComponent implements OnInit, OnChanges {
    innerTypeInfo: any[] = [];
    @Input() instanceKey: string;
    @Input() typeInfo: any[];
    @Input() typeCount: object;
    @Output() outChanged: EventEmitter<{instanceKey: string, name: string, checked: boolean}> = new EventEmitter();
    constructor() {}
    ngOnInit() {}
    ngOnChanges(simpleChanges: SimpleChanges) {
        if (simpleChanges['typeInfo'] && simpleChanges['typeInfo'].isFirstChange()) {
            this.setInnerTypeInfo();
        }
    }
    private setInnerTypeInfo() {
        this.typeInfo.forEach((info: object, index: number) => {
            const obj = {
                checked: true
            };
            Object.keys(info).forEach((key: string) => {
                obj[key] = info[key];
            });
            this.innerTypeInfo[index] = obj;
        });
        const temp = this.innerTypeInfo[0];
        this.innerTypeInfo[0] = this.innerTypeInfo[1];
        this.innerTypeInfo[1] = temp;
    }
    upperFirstChar(str: string): string {
        return str ? str.substr(0, 1).toUpperCase() + str.substr(1) : '';
    }
    getTypeCount(name: string): number {
        if (this.typeCount && this.typeCount[name]) {
            return this.typeCount[name];
        } else {
            return 0;
        }
    }
    onCheck(type: any): void {
        type.checked = !type.checked;
        this.outChanged.emit({
            instanceKey: this.instanceKey,
            name: type.name,
            checked: type.checked
        });
    }
}
