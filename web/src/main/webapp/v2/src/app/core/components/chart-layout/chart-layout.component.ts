import { Component, OnInit, Input, QueryList, ViewChildren, AfterViewInit, Output, EventEmitter } from '@angular/core';
import { CdkDragEnter, CdkDropList, moveItemInArray} from '@angular/cdk/drag-drop';

// https://stackblitz.com/edit/drag-drop-dashboard

@Component({
    selector: 'pp-chart-layout',
    templateUrl: './chart-layout.component.html',
    styleUrls: ['./chart-layout.component.css']
})
export class ChartLayoutComponent implements OnInit, AfterViewInit {
    @ViewChildren(CdkDropList) dropsQuery: QueryList<CdkDropList>;
    @Input() column: number;
    @Input() chartList: string[];
    @Output() outUpdateChartOrder: EventEmitter<string[]> = new EventEmitter();
    drops: CdkDropList[];
    constructor() {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.dropsQuery.changes.subscribe(() => {
            this.drops = this.dropsQuery.toArray();
        });
        Promise.resolve().then(() => {
            this.drops = this.dropsQuery.toArray();
        });
    }
    entered($event: CdkDragEnter) {
        moveItemInArray(this.chartList, $event.item.data, $event.container.data);
        this.outUpdateChartOrder.emit(this.chartList);
    }
    entered2($event: CdkDragEnter) {
        moveItemInArray(this.chartList, $event.item.data, $event.container.data);
    }
}
