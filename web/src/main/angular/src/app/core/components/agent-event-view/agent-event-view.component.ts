import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import * as moment from 'moment-timezone';

import { IEventStatus } from './agent-events-data.service';

@Component({
    selector: 'pp-agent-event-view',
    templateUrl: './agent-event-view.component.html',
    styleUrls: ['./agent-event-view.component.css']
})
export class AgentEventViewComponent implements OnInit {
    @Input() eventData: IEventStatus[];
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outClose = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}
    onClickClose(): void {
        this.outClose.next();
    }

    formatDate(time: number): string {
        return moment(time).tz(this.timezone).format(this.dateFormat) ;
    }
}
