import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Period } from 'app/core/models/period';

@Component({
    selector: 'pp-search-period',
    templateUrl: './search-period.component.html',
    styleUrls: ['./search-period.component.css'],
})
export class SearchPeriodComponent implements OnInit {
    @Input() periodList: Period[];
    @Input() userDefaultPeriod: Period;
    @Output() outChangeUserDefaultPeriod = new EventEmitter<Period>();

    constructor() {}
    ngOnInit() {}

    onChangeUserDefaultPeriod(value: Period): void {
        this.outChangeUserDefaultPeriod.emit(value);
    }

    compareFn(o1: Period, o2: Period): boolean {
        return o1 && o2 ? o1.equals(o2) : o1 === o2;
    }
}
