import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-inbound-outbound-range-selector-for-configuration-container',
    templateUrl: './inbound-outbound-range-selector-for-configuration-container.component.html',
    styleUrls: ['./inbound-outbound-range-selector-for-configuration-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InboundOutboundRangeSelectorForConfigurationContainerComponent implements OnInit {
    inboundList: number[];
    outboundList: number[];
    selectedInbound: number;
    selectedOutbound: number;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService
    ) {}

    ngOnInit() {
        this.inboundList = this.webAppSettingDataService.getInboundList();
        this.outboundList = this.webAppSettingDataService.getOutboundList();
        this.selectedInbound = this.webAppSettingDataService.getUserDefaultInbound();
        this.selectedOutbound = this.webAppSettingDataService.getUserDefaultOutbound();
    }

    onChangeBound(bound: number[]): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_BOUND_IN_CONFIGURATION, `Inbound: ${bound[0]}, Outbound: ${bound[1]}`);
        this.webAppSettingDataService.setUserDefaultInbound(bound[0]);
        this.webAppSettingDataService.setUserDefaultOutbound(bound[1]);
    }
}
