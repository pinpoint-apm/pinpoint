import { Component, OnInit } from '@angular/core';

import { WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-inbound-outbound-range-selector-for-configuration-popup-container',
    templateUrl: './inbound-outbound-range-selector-for-configuration-popup-container.component.html',
    styleUrls: ['./inbound-outbound-range-selector-for-configuration-popup-container.component.css']
})
export class InboundOutboundRangeSelectorForConfigurationPopupContainerComponent implements OnInit {
    inboundList: string[];
    outboundList: string[];
    selectedInbound: string;
    selectedOutbound: string;

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

    onChangeBound(bound: string[]): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_BOUND_IN_CONFIGURATION, `Inbound: ${bound[0]}, Outbound: ${bound[1]}`);
        this.webAppSettingDataService.setUserDefaultInbound(bound[0]);
        this.webAppSettingDataService.setUserDefaultOutbound(bound[1]);
    }
}
