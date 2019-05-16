import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { WebAppSettingDataService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { Subject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'pp-chart-layout-container',
    templateUrl: './chart-layout-container.component.html',
    styleUrls: ['./chart-layout-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChartLayoutContainerComponent implements OnInit {
    private unsubscribe: Subject<void> = new Subject();
    column: number;
    i18nText = {
        empty: ''
    };
    chartList: string[];
    chartState: {[key: string]: boolean};
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private messageQueueService: MessageQueueService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}
    ngOnInit() {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_MANAGER_ADD).subscribe(([chartName]: string[]) => {
            this.chartList = this.chartList.concat([chartName]);
            this.saveChartOrder();
            this.changeDetectorRef.detectChanges();
        });
        this.column = this.webAppSettingDataService.getChartLayoutOption();
        this.chartList = this.webAppSettingDataService.getChartOrderList();
        this.getI18nText();
    }
    private getI18nText(): void {
        this.translateService.get('CONFIGURATION.INSPECTOR_MANAGER.EMPTY').subscribe((text: string) => {
            this.i18nText.empty = text;
        });
    }
    onUpdateChartOrder(orderList: string[]): void {
        this.chartList = orderList;
        this.saveChartOrder();
    }
    onRemoveChart(chartName: string): void {
        this.chartList = this.chartList.filter((name: string) => {
            return name !== chartName;
        });
        this.saveChartOrder();
        this.changeDetectorRef.detectChanges();
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MANAGER_REMOVE,
            param: [chartName]
        });
    }
    private saveChartOrder(): void {
        this.webAppSettingDataService.setChartOrderList([...this.chartList]);
    }
}
