import { Component, EventEmitter, Input, Output, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { Period } from 'app/core/models/period';

@Component({
    selector: 'pp-period-selector-using-reserved-time',
    templateUrl: './period-selector-using-reserved-time.component.html',
    styleUrls: ['./period-selector-using-reserved-time.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PeriodSelectorUsingReservedTimeComponent implements OnInit {
    @Input() showRealTimeButton: boolean;
    @Input() isRealTimeMode: boolean;
    @Input() isHidden: boolean;
    @Input() periodList: Array<Period>;
    @Input() initPeriod: Period;
    @Output() outChangePeriod = new EventEmitter<Period>();

    constructor() {}
    ngOnInit() {}
    onSelectPeriod($event: any): void {
        if ($event.target.tagName.toUpperCase() === 'BUTTON') {
            this.outChangePeriod.emit($event.target.getAttribute('data-period'));
        }
    }

    isSelectedPeriod(period: Period): boolean {
        return this.isRealTimeMode === false && period.equals(this.initPeriod);
    }
}
