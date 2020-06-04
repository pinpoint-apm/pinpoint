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
    @Input() rowData: any[];
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
        return ((call[this.keyIndex.end] - call[this.keyIndex.begin]) * this.barRatio);
    }
    getLineStyle(call: any): object {
        return {
            'background-color': 'rgba(' + this.calcColor(call[this.keyIndex.applicationName]) + ', 0.1)',
        };
    }
    getStyles(call: any, i: number, j: number): object {
        return {
            'display': 'inline-block',
            'width': this.getWidth(call) + 'px',
            'background-color': 'rgb(' + this.calcColor(call[this.keyIndex.applicationName]) + ')',
            'margin-left': this.getMarginLeft(call, i, j) + 'px'
        };
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
            startTime = this.rowData[i][j-1][this.keyIndex.end];
        }
        return ((Number(call[this.keyIndex.begin]) - startTime) * this.barRatio) + offset;
    }

    onSelectCall(call: any): void {
        this.outSelectTransaction.emit(call[this.keyIndex.id]);
    }
    findParent(call:any, depth: number): number {
        let ret = -1;
        if (depth > 0) {
            if (this.rowData[depth-1].length > 0) {
                this.rowData[depth-1].forEach((candidate: any, index: number) => {
                    if (candidate[this.keyIndex.id] === call[this.keyIndex.parentId]) {
                        ret = index;
                    }
                });
            }
        }
        return ret;
    }

    showApplicationName(call: any, depth: number): boolean {
        let parentIndex = this.findParent(call, depth);
        if ((depth === 0) || (parentIndex === -1)) {
            return true;
        }

        if (this.rowData[depth - 1][parentIndex][this.keyIndex.applicationName] === call[this.keyIndex.applicationName]) {
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
        let ret: string = "";
        if (this.showApplicationName(call, depth)) {
            ret = "[" + call[this.keyIndex.applicationName] + "] ";
        }
        ret += call[this.keyIndex.apiType] + " (" + (call[this.keyIndex.end] - call[this.keyIndex.begin]) + " ms)";
        return ret;
    }
    onScrollDown() {}
}
