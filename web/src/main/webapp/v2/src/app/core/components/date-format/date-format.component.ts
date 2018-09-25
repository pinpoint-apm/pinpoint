import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import * as moment from 'moment-timezone';

@Component({
    selector: 'pp-date-format',
    templateUrl: './date-format.component.html',
    styleUrls: ['./date-format.component.css'],
})
export class DateFormatComponent implements OnInit {
    @Input() timezone: string;
    @Input() dateFormatList: string[][];
    @Input() currentDateFormatIndex: number;
    @Output() outChangeDateFormat = new EventEmitter<number>();
    private exampleTime = Date.now();
    constructor() {}
    ngOnInit() {}
    onChangeDateFormat(value: number): void {
        this.outChangeDateFormat.emit(value);
    }
    compareFn(o1: string, o2: string): boolean {
        console.log( o1, o2, o1 === o2 );
        return o1 && o2 ? o1 === o2 : false;
    }
    formatExample(date: string): string {
        return moment(this.exampleTime).tz(this.timezone).format(date);
    }
}
