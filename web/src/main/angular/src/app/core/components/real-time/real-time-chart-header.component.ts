import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-real-time-chart-header',
    templateUrl: './real-time-chart-header.component.html',
    styleUrls: ['./real-time-chart-header.component.css']
})
export class RealTimeChartHeaderComponent implements OnInit {
    @Input() activeOnly = false;
    @Input() currentPage = 1;
    @Input()
    set totalCount(totalCount: number) {
        if (this.indexLimit) {
            this.lastChartIndex = totalCount - 1 <= this.indexLimit ? totalCount - 1 : this.indexLimit;
        }

        this._totalCount = totalCount;
    }

    get totalCount(): number {
        return this._totalCount;
    }

    @Output() outOpenPage = new EventEmitter<number>();
    @Output() outChangeActiveOnlyToggle = new EventEmitter<boolean>();

    private indexLimit: number;
    private _totalCount: number;
    private maxChartNumberPerPage = 30;
    firstChartIndex: number;
    lastChartIndex: number;

    constructor() {}
    ngOnInit() {
        this.indexLimit = this.currentPage * this.maxChartNumberPerPage - 1;
        this.firstChartIndex = (this.currentPage - 1) * this.maxChartNumberPerPage;
        this.lastChartIndex = this.totalCount - 1 <= this.indexLimit ? this.totalCount - 1 : this.indexLimit;
    }

    hasChartsOnPage(): boolean {
        return this.totalCount - 1 >= this.firstChartIndex;
    }

    needPaging(): boolean {
        return this.totalCount > this.maxChartNumberPerPage;
    }

    getTotalPage(): number[] {
        const totalPage = Math.ceil(this.totalCount / this.maxChartNumberPerPage);

        return Array(totalPage).fill(0).map((v: number, i: number) => i + 1);
    }

    openPage(page: number): void {
        this.outOpenPage.emit(page);
    }

    isActivePage(page: number): boolean {
        return page === this.currentPage;
    }

    onActiveOnlyToggleChange({checked}: {checked: boolean}): void {
        this.outChangeActiveOnlyToggle.emit(checked);
    }
}
