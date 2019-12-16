import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import * as moment from 'moment-timezone';

import { Period } from 'app/core/models/period';
import { EndTime } from 'app/core/models/end-time';

@Component({
    selector: 'pp-fixed-period-mover',
    templateUrl: './fixed-period-mover.component.html',
    styleUrls: ['./fixed-period-mover.component.css']
})
export class FixedPeriodMoverComponent implements OnInit {
    @Input() period: Period;
    @Input() endTime: EndTime;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outMove = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    getStartTime(): string {
        if (this.endTime) {
            return moment(this.endTime.calcuStartTime(this.period.getValue()).getMilliSecond()).tz(this.timezone).format(this.dateFormat);
        } else {
            return '';
        }
    }

    getEndTime(): string {
        if (this.endTime) {
            return moment(this.endTime.getMilliSecond()).tz(this.timezone).format(this.dateFormat);
        } else {
            return '';
        }
    }

    onMovePrev(): void {
        this.outMove.emit(this.endTime.calcuStartTime(this.period.getValue()).getEndTime());
    }

    onMoveNext(): void {
        this.outMove.emit(this.endTime.calcuNextTime(this.period.getValue()).getEndTime());
    }
}
