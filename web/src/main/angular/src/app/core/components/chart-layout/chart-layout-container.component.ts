import { Component, OnInit, Input, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { WebAppSettingDataService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { TranslateService } from '@ngx-translate/core';
import { ChartLayoutDataService } from './chart-layout-data.service';

@Component({
    selector: 'pp-chart-layout-container',
    templateUrl: './chart-layout-container.component.html',
    styleUrls: ['./chart-layout-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChartLayoutContainerComponent implements OnInit {
    @Input() type: string;
    private unsubscribe: Subject<void> = new Subject();
    column: number;
    i18nText = {
        empty: ''
    };
    chartList: string[];
    chartState: {[key: string]: boolean};
    useDisable = true;
    showLoading = true;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private messageQueueService: MessageQueueService,
        private chartLayoutDataService: ChartLayoutDataService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}
    ngOnInit() {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_MANAGER_ADD).subscribe(({type, chartName}: {type: string, chartName: string}) => {
            if (this.type === type) {
                this.chartList = this.chartList.concat([chartName]);
                this.changeDetectorRef.detectChanges();
            }
        });
        this.chartLayoutDataService.getChartOrderList(this.type).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((list: string[]) => {
            this.column = this.webAppSettingDataService.getChartLayoutOption();
            this.chartList = list;
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        });
        this.getI18nText();
    }
    private getI18nText(): void {
        this.translateService.get('CONFIGURATION.INSPECTOR_MANAGER.EMPTY').subscribe((text: string) => {
            this.i18nText.empty = text;
        });
    }
    onUpdateChartOrder(orderList: string[]): void {
        this.chartList = orderList;
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MANAGER_CHANGE_ORDER,
            param: {
                type: this.type,
                chartOrder: [...this.chartList]
            }
        });
    }
    onRemoveChart(chartName: string): void {
        this.chartList = this.chartList.filter((name: string) => {
            return name !== chartName;
        });
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MANAGER_REMOVE,
            param: {
                type: this.type,
                chartName
            }
        });
        this.changeDetectorRef.detectChanges();
    }
    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }
    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }
}
