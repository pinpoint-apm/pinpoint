import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-inspector-chart-list',
    templateUrl: './inspector-chart-list.component.html',
    styleUrls: ['./inspector-chart-list.component.css']
})
export class InspectorChartListComponent implements OnInit {
    @Input() chartList: string[];
    @Input() chartState: {[key: string]: boolean};
    @Input() emptyText: string;
    @Output() outAddChart = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    onSelectChart(chart: string): void {
        if (this.chartState[chart] === false) {
            this.outAddChart.emit(chart);
        }
    }
    isEmpty(): boolean {
        return this.chartList && this.chartList.length === 0;
    }
}
