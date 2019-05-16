import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { WebAppSettingDataService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';

@Component({
    selector: 'pp-inspector-chart-list-container',
    templateUrl: './inspector-chart-list-container.component.html',
    styleUrls: ['./inspector-chart-list-container.component.css']
})
export class InspectorChartListContainerComponent implements OnInit {
    private unsubscribe: Subject<void> = new Subject();
    chartList: string[];
    chartState: {[key: string]: boolean};
    constructor(
        private messageQueueService: MessageQueueService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}

    ngOnInit() {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_MANAGER_REMOVE).subscribe(([chartName]: string[]) => {
            this.chartState = {
                ...this.chartState,
                [chartName]: false
            };
            this.saveChartState();
        });
        this.chartList = this.webAppSettingDataService.getChartDefaultOrderList();
        this.chartState = this.webAppSettingDataService.getChartVisibleState();
        this.getI18nText();
    }
    private getI18nText(): void {
    }
    onAddChart(chartName: string): void {
        this.chartState = {
            ...this.chartState,
            [chartName]: true
        };
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MANAGER_ADD,
            param: [chartName]
        });
        this.saveChartState();
    }
    private saveChartState(): void {
        this.webAppSettingDataService.setChartVisibleState({
            ...this.chartState
        });
    }
}
