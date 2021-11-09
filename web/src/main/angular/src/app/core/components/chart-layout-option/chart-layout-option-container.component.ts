import { Component, OnInit } from '@angular/core';

import { WebAppSettingDataService, MessageQueueService, AnalyticsService, MESSAGE_TO, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-chart-layout-option-container',
    templateUrl: './chart-layout-option-container.component.html',
    styleUrls: ['./chart-layout-option-container.component.css']
})
export class ChartLayoutOptionContainerComponent implements OnInit {
    storedChartLayoutOption: number;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.storedChartLayoutOption = this.webAppSettingDataService.getChartLayoutOption();
    }

    onClickOption(chartNumPerRow: number): void {
        this.webAppSettingDataService.setChartLayoutOption(chartNumPerRow);
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SET_CHART_LAYOUT,
            param: chartNumPerRow
        });
        // TODO: 현재의 구조로는 어느한쪽(inspector 또는 metric)의 레이아웃 설정이 다른쪽에 영향을 주는데 이걸 어떻게 풀어내야할까.
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_INSPECTOR_CHART_LAYOUT_OPTION, `Chart Number Per Row: ${chartNumPerRow}`);
    }
}
