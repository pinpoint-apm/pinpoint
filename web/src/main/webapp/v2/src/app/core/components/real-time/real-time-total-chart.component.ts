import { Component, OnInit, Input } from '@angular/core';
import { IRealTimeChartData } from './real-time-chart.component';

@Component({
    selector: 'pp-real-time-total-chart',
    templateUrl: './real-time-total-chart.component.html',
    styleUrls: ['./real-time-total-chart.component.css']
})
export class RealTimeTotalChartComponent implements OnInit {
    @Input() applicationName: string;
    @Input() hasError: boolean;
    @Input() errorMessage: string;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() chartData: IRealTimeChartData;

    showAxis = true;
    constructor() {}
    ngOnInit() {}
    getTotalCount(): number {
        return this.chartData.responseCount.reduce((prev: number, curr: number) => prev + curr, 0);
    }
}
