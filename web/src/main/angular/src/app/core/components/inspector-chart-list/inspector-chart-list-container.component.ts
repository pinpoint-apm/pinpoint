import { Component, OnInit, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { InspectorChartListDataService } from './inspector-chart-list-data.service';

@Component({
    selector: 'pp-inspector-chart-list-container',
    templateUrl: './inspector-chart-list-container.component.html',
    styleUrls: ['./inspector-chart-list-container.component.css']
})
export class InspectorChartListContainerComponent implements OnInit {
    @Input() type: string;
    private unsubscribe: Subject<void> = new Subject();
    chartList: string[];
    chartState: {[key: string]: boolean};
    useDisable = true;
    showLoading = true;
    constructor(
        private messageQueueService: MessageQueueService,
        private inspectorChartListDataService: InspectorChartListDataService,
    ) {}

    ngOnInit() {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_MANAGER_REMOVE).subscribe(({type, chartName}: {type: string, chartName: string}) => {
            if (this.type === type) {
                this.chartState = {
                    ...this.chartState,
                    [chartName]: false
                };
                this.saveChartState();
            }
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_MANAGER_CHANGE_ORDER).subscribe(({type, chartOrder}: {type: string, chartOrder: string[]}) => {
            if (this.type === type) {
                this.saveChartOrder(chartOrder);
            }
        });
        this.inspectorChartListDataService.getChartVisibleState(this.type).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((state: {[key: string]: boolean}) => {
            this.chartList = this.inspectorChartListDataService.getDefaultChartList(this.type);
            this.chartState = state;
            this.hideProcessing();
        });
    }
    private saveChartState(): void {
        this.inspectorChartListDataService.setChartVisibleState(this.type, {
            ...this.chartState
        });
    }
    private saveChartOrder(chartOrder: string[]): void {
        this.inspectorChartListDataService.setChartOrderState(this.type, chartOrder);
    }
    onAddChart(chartName: string): void {
        this.showProcessing();
        this.chartState = {
            ...this.chartState,
            [chartName]: true
        };
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MANAGER_ADD,
            param: {
                type: this.type,
                chartName
            }
        });
        this.saveChartState();
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
