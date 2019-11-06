import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-transaction-timeline',
    templateUrl: './transaction-timeline.component.html',
    styleUrls: ['./transaction-timeline.component.css']
})

export class TransactionTimelineComponent implements OnInit {
    @Input() data: any[];
    @Input() keyIndex: { [key: string]: number};
    @Input() startTime: number;
    @Input() endTime: number;
    @Input() barRatio: number;
    @Output() outSelectTransaction: EventEmitter<string> = new EventEmitter();

    selectedRow: number[] = [];
    colorSet: { [key: string]: string } = {};
    constructor() {}
    ngOnInit() {}
    private calcColor(str: string): string {
        if (!(str in this.colorSet)) {
            const color = [];
            let hash = 0;
            for ( let i = 0 ; i < str.length ; i++ ) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            for ( let i = 0 ; i < 3 ; i++ ) {
                color.push(('00' + ((hash >> i * 8) & 0xFF).toString(16)).slice(-2));
            }
            this.colorSet[str] = color.map((v: string) => {
                return parseInt(v, 16);
            }).join(',');
        }
        return this.colorSet[str];
    }
    private getWidth(call: any): number {
        return ((call[this.keyIndex.end] - call[this.keyIndex.begin]) * this.barRatio) + 0.9;
    }
    getLineStyle(call: any): object {
        return {
            'background-color': 'rgba(' + this.calcColor(call[this.keyIndex.applicationName]) + ', 0.1)',
        };
    }
    getStyles(call: any): object {
        return {
            'width': this.getWidth(call) + 'px',
            'background-color': 'rgb(' + this.calcColor(call[this.keyIndex.applicationName]) + ')',
            'margin-left': this.getMarginLeft(call) + 'px'
        };
    }
    getTop(index: number): number {
        return index * 21;
    }
    isSelectedRow(index: number): boolean {
        return this.selectedRow.indexOf(index) !== -1;
    }
    getStartTime(call: any): number {
        return call[this.keyIndex.begin] - this.startTime;
    }

    getMarginLeft(call: any): number {
        return ((call[this.keyIndex.begin] - this.startTime) * this.barRatio) + 0.9;
    }
    onSelectCall(index: number, call: any): void {
        this.outSelectTransaction.emit(call[this.keyIndex.id]);
    }
    searchRow({type, query}: {type: string, query: string | number}): number {
        let resultCount = 0;
        this.selectedRow = [];
        const fnCompare = {
            'self': (data: any, value: string): boolean => {
                return data[this.keyIndex.executionMilliseconds] >= value;
            },
            'argument': (data: any, value: number): boolean => {
                return data[this.keyIndex.arguments].indexOf(value) !== -1;
            }
        };
        this.data.forEach((call: any, index: number) => {
            if (fnCompare[type](call, query)) {
                resultCount++;
                this.selectedRow.push(index);
            }
        });
        if (resultCount > 0) {
            // move row
        }
        return resultCount;
    }
    showApplicationName(call: any, index: number): boolean {
        if (index === 0 || index >= this.data.length) {
            return true;
        } else {
            if (this.data[index - 1][this.keyIndex.applicationName] === call[this.keyIndex.applicationName]) {
                return false;
            } else {
                return true;
            }
        }
    }
    onScrollDown() {}
}
