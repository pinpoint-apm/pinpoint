import { Component, OnInit, OnChanges, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';

import { Period } from 'app/core/models/period';
import { EndTime } from 'app/core/models/end-time';
import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

enum PeriodSelectType {
    RESERVED_PERIOD,
    CALENDAR_PERIOD
}

@Component({
    selector: 'pp-period-selector',
    templateUrl: './period-selector.component.html',
    styleUrls: ['./period-selector.component.css']
})
export class PeriodSelectorComponent implements OnInit, OnChanges {
    @Input() i18nText: any;
    @Input() showRealTimeButton: boolean;
    @Input() isRealTimeMode: boolean;
    @Input() selectedPeriod: Period;
    @Input() selectedEndTime: EndTime;
    @Input() periodList: Period[];
    @Input() maxPeriod: number;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outChangePeriod = new EventEmitter<string>();
    @Output() outChangeCalendarTime = new EventEmitter<any>();

    isReservedType: boolean;

    constructor(
        private analyticsService: AnalyticsService,
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes['selectedPeriod'] && changes['selectedEndTime'] || changes['isRealTimeMode']) {
            this.checkPeriodType();
        }
    }

    ngOnInit() {}

    set periodSelectType(type: PeriodSelectType) {
        this.isReservedType = type === PeriodSelectType.RESERVED_PERIOD;
    }

    onChangePeriodTime(selectedPeriod: string): void {
        this.outChangePeriod.emit(selectedPeriod);
    }

    onChangeCalendarTime(oChangeTime: any): void {
        if (this.selectedEndTime.equals(oChangeTime.endTime) === false || this.selectedPeriod.equals(oChangeTime.period) === false) {
            this.outChangeCalendarTime.emit(oChangeTime);
        }
    }

    private checkPeriodType(): void {
        if (this.isRealTimeMode) {
            this.periodSelectType = PeriodSelectType.RESERVED_PERIOD;
            return;
        }
        for (let i = 0; i < this.periodList.length; i++) {
            if (this.periodList[i].equals(this.selectedPeriod)) {
                this.periodSelectType = PeriodSelectType.RESERVED_PERIOD;
                return;
            }
        }
        this.periodSelectType = PeriodSelectType.CALENDAR_PERIOD;
    }

    changeToReservedType(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_PERIOD_SELECT_TYPE, PeriodSelectType[PeriodSelectType.RESERVED_PERIOD]);
        this.periodSelectType = PeriodSelectType.RESERVED_PERIOD;
    }

    changeToCalendarType(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_PERIOD_SELECT_TYPE, PeriodSelectType[PeriodSelectType.CALENDAR_PERIOD]);
        this.periodSelectType = PeriodSelectType.CALENDAR_PERIOD;
    }
}
