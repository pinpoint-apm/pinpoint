import { Component, Input, Output, OnInit, OnChanges, SimpleChanges, EventEmitter, ViewEncapsulation } from '@angular/core';
import * as moment from 'moment-timezone';

import { Period } from 'app/core/models/period';
import { EndTime } from 'app/core/models/end-time';

@Component({
    selector: 'pp-period-selector-using-calendar',
    templateUrl: './period-selector-using-calendar.component.html',
    styleUrls: ['./period-selector-using-calendar.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PeriodSelectorUsingCalendarComponent implements OnInit, OnChanges {
    @Input() isHidden: boolean;
    @Input() i18nText: any;
    @Input() maxPeriod: number;
    @Input() periodList: Array<Period>;
    @Input() initPeriod: Period;
    @Input() initEndTime: EndTime;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outChangePeriod = new EventEmitter<any>();

    isPopupHidden = true;
    startDate = moment();
    endDate = moment();
    minDate = new Date(1977, 0, 1);
    maxDate = new Date();
    hours: Array<any> = Array(24).fill('').map(function (v, i) {
        return {
            value: i,
            display: ('0' + i).substr(-2)
        };
    });
    minutes: Array<any> = Array(60).fill('').map(function (v, i) {
        return {
            value: i,
            display: ('0' + i).substr(-2)
        };
    });
    wrongDate = false;
    constructor() {}
    ngOnChanges(changes: SimpleChanges) {
        if ((changes['initEndTime'] && changes['initEndTime']['currentValue']) && (changes['initPeriod'] && changes['initPeriod']['currentValue'])) {
            this.startDate = moment(this.initEndTime.calcuStartTime(this.initPeriod.getValue()).getDate()).tz(this.timezone);
            this.endDate = moment(this.initEndTime.getDate()).tz(this.timezone);
        }
    }
    ngOnInit() {}
    getStartTime(): string {
        return this.startDate.format('HH:mm Z');
    }
    getEndTime(): string {
        return this.endDate.format('HH:mm Z');
    }
    getStartDate(): string {
        return this.startDate.format(this.dateFormat);
    }
    getEndDate(): string {
        return this.endDate.format(this.dateFormat);
    }
    checkTimeValid(): void {
        if (this.endDate.isBefore(this.startDate) || this.endDate.diff(this.startDate, 'minute') > this.maxPeriod) {
            this.wrongDate = true;
        } else {
            this.wrongDate = false;
        }
    }
    onChangedStartDate(date: Date): void {
        this.startDate.set({
            'year': date.getFullYear(),
            'month': date.getMonth(),
            'date': date.getDate()
        });
        this.checkTimeValid();
    }
    onChangedEndDate(date: Date): void {
        this.endDate.set({
            'year': date.getFullYear(),
            'month': date.getMonth(),
            'date': date.getDate()
        });
        this.checkTimeValid();
    }
    onClosePopup(): void {
        this.isPopupHidden = true;
    }
    onTogglePopup(): void {
        this.isPopupHidden = !this.isPopupHidden;
    }
    onChangeStartHour(val: string): void {
        this.startDate.hour(parseInt(val, 10));
        this.checkTimeValid();
    }
    onChangeStartMinute(val: string): void {
        this.startDate.minute(parseInt(val, 10));
        this.checkTimeValid();
    }
    onChangeEndHour(val: string): void {
        this.endDate.hour(parseInt(val, 10));
        this.checkTimeValid();
    }
    onChangeEndMinute(val: string): void {
        this.endDate.minute(parseInt(val, 10));
        this.checkTimeValid();
    }
    onChangeToReservedPeriod($event: any): void {
        if ($event.target.tagName.toUpperCase() === 'BUTTON') {
            this.startDate = this.endDate.clone().subtract(Period.parseToMinute($event.target.getAttribute('data-period')), 'minute');
            this.checkTimeValid();
        }
    }
    onSelectPeriod(): void {
        if (this.wrongDate === false) {
            this.onClosePopup();
            this.outChangePeriod.emit({
                period: new Period(this.endDate.diff(this.startDate, 'minute')),
                endTime: EndTime.newByNumber(this.endDate.valueOf())
            });
        }
    }
}
