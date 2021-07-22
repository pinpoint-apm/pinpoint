import { Component, Input, OnInit, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';

@Component({
    selector: 'pp-url-statistic-info',
    templateUrl: './url-statistic-info.component.html',
    styleUrls: ['./url-statistic-info.component.css']
})
export class UrlStatisticInfoComponent implements OnInit, OnChanges {
    @Input() data: any[];
    @Output() outSelectUrl = new EventEmitter<string>();

    private totalCount: number;

    selectedUrl: string;

    constructor() {}
    ngOnChanges(changes: SimpleChanges) {
        const dataChange = changes['data'];

        if (dataChange && dataChange.currentValue) {
            this.selectedUrl = dataChange.currentValue[0].uri;
            this.totalCount = dataChange.currentValue.reduce((acc: number, {totalCount}: any) => {
                return acc + totalCount;
            }, 0);
        }
    }

    ngOnInit() {}
    onSelectUrl(url: string): void {
        if (this.selectedUrl === url) {
            return;
        }

        this.selectedUrl = url;
        this.outSelectUrl.emit(url);
    }

    isSelectedUrl(url: string): boolean {
        return this.selectedUrl === url;
    }

    getRatioBackgroundColor(count: number): string {
        return `linear-gradient(to right, #DAE6F0 ${count / this.totalCount * 100}%, #F2F2F2 ${count / this.totalCount * 100}% 100%)`;
    }
}
