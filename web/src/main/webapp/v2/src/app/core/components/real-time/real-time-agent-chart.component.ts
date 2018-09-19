import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { IRealTimeChartData } from './real-time-chart.component';

@Component({
    selector: 'pp-real-time-agent-chart',
    templateUrl: './real-time-agent-chart.component.html',
    styleUrls: ['./real-time-agent-chart.component.css']
})
export class RealTimeAgentChartComponent implements OnInit {
    @Input() agentName: string;
    @Input() hasError: boolean;
    @Input() errorMessage: string;
    @Input() chartData: IRealTimeChartData;
    @Output() outOpenThreadDump: EventEmitter<string> = new EventEmitter();

    showAxis = false;

    constructor() {}
    ngOnInit() {}
    onOpen(): void {
        this.outOpenThreadDump.emit(this.agentName);
    }
}
