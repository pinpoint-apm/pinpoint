import { Component, OnInit } from '@angular/core';
import { WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-chart-layout-container',
    templateUrl: './chart-layout-container.component.html',
    styleUrls: ['./chart-layout-container.component.css']
})
export class ChartLayoutContainerComponent implements OnInit {
    column: number;
    chartList: string[];
    constructor(
        private webAppSettingDataService: WebAppSettingDataService
    ) {}
    ngOnInit() {
        this.column = this.webAppSettingDataService.getChartLayoutOption();
        this.chartList = this.webAppSettingDataService.getChartOrderList();
    }
    onUpdateChartOrder(orderList: string[]): void {
        this.webAppSettingDataService.setChartOrderList(orderList);
    }
}
