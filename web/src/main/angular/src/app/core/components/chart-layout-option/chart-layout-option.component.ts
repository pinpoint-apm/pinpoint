import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';

@Component({
    selector: 'pp-chart-layout-option',
    templateUrl: './chart-layout-option.component.html',
    styleUrls: ['./chart-layout-option.component.css']
})
export class ChartLayoutOptionComponent implements OnInit {
    @Input() chartNumPerRow: number;
    @Output() outClickOption = new EventEmitter<number>();

    private activeOption: number;

    constructor() {}
    ngOnInit() {
        this.activeOption = this.chartNumPerRow;
    }

    onClickOption(chartNumPerRow: number): void {
        if (chartNumPerRow !== this.activeOption) {
            this.activeOption = chartNumPerRow;
            this.outClickOption.emit(chartNumPerRow);
        }
    }

    isActive(chartNumbPerRow: number): boolean {
        return this.activeOption === chartNumbPerRow;
    }
}
