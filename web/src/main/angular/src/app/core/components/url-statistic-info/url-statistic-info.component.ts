import { Component, Input, OnInit, Output, EventEmitter, SimpleChanges, OnChanges, ElementRef } from '@angular/core';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-url-statistic-info',
    templateUrl: './url-statistic-info.component.html',
    styleUrls: ['./url-statistic-info.component.css']
})
export class UrlStatisticInfoComponent implements OnInit, OnChanges {
    @Input() data: IUrlStatInfoData[];
    @Output() outSelectUrlInfo = new EventEmitter<string>();

    totalCount: number;
    selectedUrl: string;
    isEmpty: boolean;

    constructor(
        private el: ElementRef
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        const dataChange = changes['data'];
        
        if (dataChange && dataChange.currentValue) {
            this.isEmpty = isEmpty(dataChange.currentValue);
            if (this.isEmpty) {
                return;
            }

            // this.selectedUrl = dataChange.currentValue[0].uri;
            this.selectedUrl = '';
            this.totalCount = dataChange.currentValue.reduce((acc: number, {totalCount}: any) => {
                return acc + totalCount;
            }, 0);
        }
    }

    ngOnInit() {}
    onSelectUrlInfo(url: string): void {
        if (this.selectedUrl === url) {
            return;
        }

        this.selectedUrl = url;
        this.outSelectUrlInfo.emit(url);
    }

    isSelectedUrl(url: string): boolean {
        return this.selectedUrl === url;
    }

    getRatioBackgroundColor(count: number): string {
        const computedStyle = getComputedStyle(this.el.nativeElement);
        const urlCountRatio = computedStyle.getPropertyValue('--url-count-ratio');
        const urlCountRatioBG = computedStyle.getPropertyValue('--url-count-ratio-background');

        return `linear-gradient(to right, ${urlCountRatio} ${count / this.totalCount * 100}%, ${urlCountRatioBG} ${count / this.totalCount * 100}% 100%)`;
    }
}
