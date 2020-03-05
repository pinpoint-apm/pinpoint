import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-scatter-chart-options',
    templateUrl: './scatter-chart-options.component.html',
    styleUrls: ['./scatter-chart-options.component.css']
})
export class ScatterChartOptionsComponent implements OnInit {
    @Output() outShowSetting: EventEmitter<null> = new EventEmitter();
    @Output() outDownload: EventEmitter<null> = new EventEmitter();
    @Output() outOpenScatterPage: EventEmitter<null> = new EventEmitter();
    @Output() outShowHelp: EventEmitter<MouseEvent> = new EventEmitter();
    @Input() instanceKey: string;
    @Input() hiddenOptions: { setting: boolean, download: boolean, open: boolean, help: boolean };
    constructor() {}
    ngOnInit() {}
    onShowHelp($event: MouseEvent): void {
        this.outShowHelp.emit($event);
    }
}
