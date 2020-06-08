import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-transaction-timeline-v2',
    templateUrl: './transaction-timeline-v2.component.html',
    styleUrls: ['./transaction-timeline-v2.component.css']
})

export class TransactionTimelineComponentV2 implements OnInit {
    @Input() keyIndex: {[key: string]: number};
    @Input() startTime: number;
    @Input() endTime: number;
    @Input() barRatio: number;
    @Input() rowData: any;
    @Output() outSelectTransaction = new EventEmitter<string>();

    colorSet: { [key: string]: string } = {};
    constructor() {}
    ngOnInit() {
        this.rowData = this.rowData.filter(row => row.length > 0);
    }
    private calcColor(str: string): string {
        if (!(str in this.colorSet)) {
            const color = [];
            let hash = 0;
            for (let i = 0; i < str.length; i++) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            for (let i = 0; i < 3; i++) {
                color.push(('00' + ((hash >> i * 8) & 0xFF).toString(16)).slice(-2));
            }
            this.colorSet[str] = color.map((v: string) => {
                return parseInt(v, 16);
            }).join(',');
        }
        return this.colorSet[str];
    }

    private getWidth(call: any): number {
        let width = (call[this.keyIndex.end] - call[this.keyIndex.begin]) * this.barRatio;
        return (width < 1 ? 0 : width);
    }

    getLineStyle(row: any): object {
        return {
            'background-color': 'rgba(' + this.calcColor(row[0][this.getDataIndex(row[0])][this.keyIndex.applicationName]) + ', 0.1)',
        };
    }

    getStyles(call: any, i: number, j: number): object {
        let dataIndex = this.getDataIndex(call);
        let color = this.calcColor(call[dataIndex][this.keyIndex.applicationName]);
        if (dataIndex == 0) {
            // sync
            return {
                'display': 'inline-block',
                'width': this.getWidth(call[dataIndex]) + 'px',
                'background-color': 'rgb(' + color + ')',
                'margin-left': this.getMarginLeft(call[dataIndex], i, j) + 'px'
            };
        } else {
            // async
            return {
                'display': 'inline-block',
                'width': this.getWidth(call[dataIndex]) + 'px',
                'margin-left': this.getMarginLeft(call[dataIndex], i, j) + 'px',
                'background-color': 'rgb('+color+')',
                'height': '50%',
                'background': 'repeating-linear-gradient(45deg, transparent, transparent 1px, rgb(' + color + ') 1px, rgb(' + color + ') 2px),' +
                              ' linear-gradient(to bottom, rgba(' + color + ', 0.3), rgba(' + color + ', 0.3))'
            };
        }
    }
    getStartTime(call: any): number {
        return call[this.keyIndex.begin] - this.startTime;
    }

    getMarginLeft(call: any, i: number, j: number): number {
        let startTime: number;
        let offset = 0;

        if ((j === 0)) {
            startTime = Number(this.startTime);
            offset = 1;

        } else {
            let prev = this.rowData[i][j-1];
            startTime = Number(prev[this.getDataIndex(prev)][this.keyIndex.end])
        }

        return ((Number(call[this.keyIndex.begin]) - startTime) * this.barRatio) + offset;
    }

    onSelectCall(call: any): void {
        this.outSelectTransaction.emit(call[this.keyIndex.id]);
    }

    getDataIndex(call: any): number {
        return (call[0]==null)? 1: 0;
    }

    findParent(call:any, depth: number): any {
        let ret = null;
        if (depth > 0) {
            if (this.rowData[depth-1].length > 0) {
                this.rowData[depth-1].forEach((candidate: any, index: number) => {
                    if (candidate[this.getDataIndex(candidate)][this.keyIndex.id] === call[this.keyIndex.parentId]) {
                        ret = candidate[this.getDataIndex(candidate)];
                    }
                });
            }
        }
        return ret;
    }

    showApplicationName(call: any, depth: number): boolean {
        let parent = this.findParent(call, depth);
        if ((depth === 0) || (parent === null)) {
            return true;
        }

        if (parent[this.keyIndex.applicationName] === call[this.keyIndex.applicationName]) {
            return false;
        } else {
            return true;
        }
    }
    showName(call: any): boolean {
        if (this.getWidth(call) > 180) {
            return true;
        } else {
            return false;
        }
    }
    getText(call: any, depth: number): string {
        let dataIndex = this.getDataIndex(call);
        let ret: string = "";
        if (this.showApplicationName(call[dataIndex], depth)) {
            ret = "[" + call[dataIndex][this.keyIndex.applicationName] + "] ";
        }
        ret += call[dataIndex][this.keyIndex.apiType] + " (" + (call[dataIndex][this.keyIndex.end] - call[dataIndex][this.keyIndex.begin]) + " ms)";

        if (dataIndex === 1) {
            ret += " / Asynchronous"
        }
        return ret;
    }
    onScrollDown() {}
}
