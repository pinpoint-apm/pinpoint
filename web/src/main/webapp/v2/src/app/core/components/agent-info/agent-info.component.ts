import { Component, OnInit, Input, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import * as moment from 'moment-timezone';

import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-agent-info',
    templateUrl: './agent-info.component.html',
    styleUrls: ['./agent-info.component.css']
})
export class AgentInfoComponent implements OnInit, OnChanges {
    @Input() urlApplicationName: string;
    @Input() agentData: IServerAndAgentData;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outClickApplicationNameIssue = new EventEmitter<{[key: string]: any}>();

    isHideDetailInfo = true;
    selectedServiceIndex = 0;

    constructor(
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes)
            .filter((propName: string) => {
                return changes[propName].currentValue;
            })
            .forEach((propName: string) => {
                switch (propName) {
                    case 'agentData':
                        this.isHideDetailInfo = true;
                        this.selectedServiceIndex = 0;
                        break;
                }
            });
    }
    toggleDetailInfo(): boolean {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_SERVER_TYPE_DETAIL);
        return this.isHideDetailInfo = !this.isHideDetailInfo;
    }
    onSelectService(index: number): void {
        this.selectedServiceIndex = index;
    }
    getSelectedServiceLib(): string[] {
        return this.agentData.serverMetaData.serviceInfos[this.selectedServiceIndex].serviceLibs;
    }
    isServiceInfoEmpty(): boolean {
        return !this.agentData.serverMetaData || this.agentData.serverMetaData.serviceInfos.length === 0;
    }
    isSameApplication(): boolean {
        return this.agentData.applicationName === this.urlApplicationName;
    }
    formatDate(time: number): string {
        return moment(time).tz(this.timezone).format(this.dateFormat) ;
    }
    onClickNotSameBtn($event: MouseEvent): void {
        const {left, top, width, height} = ($event.currentTarget as HTMLElement).getBoundingClientRect();
        const { agentId, applicationName } = this.agentData;

        this.outClickApplicationNameIssue.emit({
            data: { agentId, applicationName },
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            }
        });
    }
}
