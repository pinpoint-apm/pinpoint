import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-timezone-select',
    templateUrl: './timezone-select.component.html',
    styleUrls: ['./timezone-select.component.css'],
})
export class TimezoneSelectComponent implements OnInit {
    @Input() timezoneList: string[];
    @Input() currentTimezone: string;
    @Output() outChangeTimezone = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {
    }

    onChangeTimezone(value: string): void {
        this.outChangeTimezone.emit(value);
    }

    compareFn(o1: string, o2: string): boolean {
        return o1 && o2 ? o1 === o2 : false;
    }
}
