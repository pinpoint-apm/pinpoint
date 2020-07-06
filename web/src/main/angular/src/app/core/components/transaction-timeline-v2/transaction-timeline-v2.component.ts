import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-transaction-timeline-v2',
    templateUrl: './transaction-timeline-v2.component.html',
    styleUrls: ['./transaction-timeline-v2.component.css']
})

export class TransactionTimelineComponentV2 implements OnInit {
    @Input() startTime: number;
    @Input() endTime: number;
    @Input() barRatio: number;
    @Input() rowData: any;
    @Input() databaseCalls: any;
    @Output() outSelectTransaction = new EventEmitter<string>();

    colorSet: { [key: string]: string } = {};
    bgColor: String;
    constructor() {}
    ngOnInit() {
        this.rowData = this.rowData.filter(row => row.length > 0);
        this.bgColor = 'rgba(' + this.calcColor(this.rowData[0][0][0].applicationName) + ', 0.1)';
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
        let width = (call.end - call.begin) * this.barRatio;
        return (width < 1 ? 0 : width);
    }

    private getLeft(call: any): number {
        return ((Number(call.begin) - Number(this.startTime)) * this.barRatio) + 1;
    }

    private findParent(call:any, depth: number): any {
        let ret = null;
        if (depth > 0) {
            if (this.rowData[depth-1].length > 0) {
                this.rowData[depth-1].forEach((candidate: any, index: number) => {
                    if (candidate[this.getDataIndex(candidate)].id === call.parentId) {
                        ret = candidate[this.getDataIndex(candidate)];
                    }
                });
            }
        }
        return ret;
    }

    private showApplicationName(call: any, depth: number): boolean {
        let parent = this.findParent(call, depth);
        if (parent === null) {
            return true;
        }

        if (parent.applicationName === call.applicationName) {
            return false;
        } else {
            return true;
        }
    }

    getLineStyle(): object {
        return {
            'background-color': this.bgColor,
        };
    }

    getStyles(call: any, isAsync: boolean): object {
        let color = this.calcColor(call.applicationName);
        let ret = {
            'display': 'inline-block',
            'position': 'absolute',
            'width': this.getWidth(call) + 'px',
            'background-color': 'rgb(' + color + ')',
            'left': this.getLeft(call) + 'px'

        };
        if (isAsync) {
            ret["height"] = '10px';
            ret["background"] = 'repeating-linear-gradient(45deg, transparent, transparent 1px, rgb(' + color + ') 1px, rgb(' + color + ') 2px),' +
                              ' linear-gradient(to bottom, rgba(' + color + ', 0.3), rgba(' + color + ', 0.3))';
        }
        if (call.apiType === "ASYNC") {
            ret["border-top"] = '2px solid rgb(' + color + ')';
        }
        return ret;
    }

    onSelectCall(call: any): void {
        this.outSelectTransaction.emit(call.id);
    }

    getDataIndex(call: any): number {
        return (call[0]==null)? 1: 0;
    }

    showName(call: any, isAsync: boolean): boolean {
        if ((this.getWidth(call) > 180) && !isAsync) {
            return true;
        }
        return false;
    }

    getText(call: any, depth: number, isAsync: boolean): string {
        let ret: string = "";
        if (this.showApplicationName(call, depth)) {
            ret = "[" + call.applicationName + "] ";
        }
        ret += call.apiType + " (" + (call.end - call.begin) + " ms)";

        if (isAsync) {
            ret += " / Asynchronous"
        }
        return ret;
    }

    onScrollDown() {}
}
