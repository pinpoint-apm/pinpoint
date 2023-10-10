import { Component, Input, OnInit, Output, EventEmitter, SimpleChanges, OnChanges, ElementRef, Renderer2, ViewChild, AfterViewChecked } from '@angular/core';

import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-url-statistic-info',
    templateUrl: './url-statistic-info.component.html',
    styleUrls: ['./url-statistic-info.component.css']
})
export class UrlStatisticInfoComponent implements OnInit, OnChanges, AfterViewChecked {
    @ViewChild('urlInfoTableBody' , {static: true}) urlInfoTableBody: ElementRef;
    @Input() data: IUrlStatInfoData[];
    @Input() sortStatus: {orderby: string, isDesc: boolean};
    @Input() emptyMessage: string;
    @Output() outSelectUrlInfo = new EventEmitter<string>();
    @Output() outClickSort = new EventEmitter<{orderby: string, isDesc: boolean}>();

    private dataUpdated: boolean;

    columnList = [
        {
            key: 'index',
            headerLabel: '#', 
            headerClassName: this.getHeaderCellClassName('index-cell'),
        },
        {
            key: 'uri',
            headerLabel: 'URL',
            headerClassName: this.getHeaderCellClassName('url-cell'),
        },
        {
            key: 'totalCount',
            headerLabel: 'Total Count',
            headerClassName: this.getHeaderCellClassName('total-count-cell'),
        },
        {
            key: 'failureCount',
            headerLabel: 'Failure Count',
            headerClassName: this.getHeaderCellClassName('failure-count-cell')
        },
        {
            key: 'apdex',
            headerLabel: 'Apdex',
            headerClassName: this.getHeaderCellClassName('apdex-cell')
        },
        {
            key: 'avgTimeMs',
            headerLabel: 'Avg(ms)',
            headerClassName: this.getHeaderCellClassName('avg-cell')
        },
        {
            key: 'maxTimeMs',
            headerLabel: 'Max(ms)',
            headerClassName: this.getHeaderCellClassName('max-cell')
        },
        
    ]

    totalCount: number;
    selectedUrl: string;
    isEmpty: boolean;

    constructor(
        private el: ElementRef,
        private renderer: Renderer2,
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        const dataChange = changes['data'];
        
        if (dataChange && dataChange.currentValue) {
            const data = dataChange.currentValue as IUrlStatInfoData[];

            this.isEmpty = isEmpty(data);
            if (this.isEmpty) {
                this.selectedUrl = '';
                this.outSelectUrlInfo.emit(null);
                return;
            }

            this.dataUpdated = true;
            const prevSelectedUrlInfo = data.find(({uri}: IUrlStatInfoData) => this.selectedUrl === uri);

            if (!prevSelectedUrlInfo) {
                this.selectedUrl = ''; 
            } 

            this.outSelectUrlInfo.emit(this.selectedUrl);

            this.totalCount = data.reduce((acc: number, {totalCount}: any) => {
                return acc + totalCount;
            }, 0);
        }
    }

    ngOnInit() {
        this.renderer.setStyle(this.urlInfoTableBody.nativeElement, 'max-height', `${this.el.nativeElement.offsetHeight - 40}px`);
    }

    ngAfterViewChecked() {
        if (this.dataUpdated) {
            const elementId = this.selectedUrl ? this.selectedUrl : this.data[0].uri;

            document.getElementById(elementId).scrollIntoView({
                behavior: 'auto',
                block: 'center',
            });

            this.dataUpdated = false;
        }
    }

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

    getFormattedApdexScore(score: number): string {
        return (Math.floor(score * 100) / 100).toFixed(2);
    }

    getRatioBackgroundColor(count: number): string {
        const computedStyle = getComputedStyle(this.el.nativeElement);
        const urlCountRatio = computedStyle.getPropertyValue('--url-count-ratio');
        const urlCountRatioBG = computedStyle.getPropertyValue('--url-count-ratio-background');

        return `linear-gradient(to right, ${urlCountRatio} ${count / this.totalCount * 100}%, ${urlCountRatioBG} ${count / this.totalCount * 100}% 100%)`;
    }

    onClickSort(key: string): void {
        this.outClickSort.next({
            orderby: key,
            isDesc: this.sortStatus.orderby === key ? !this.sortStatus.isDesc : true
        });
    }

    isSortActive(key: string): boolean {
        return key === this.sortStatus.orderby;
    }

    getSortIconClass(): string {
        return this.sortStatus.isDesc ? 'fas fa-arrow-down' : 'fas fa-arrow-up'
    }

    private getHeaderCellClassName(className: string): string {
        const commonClassNameList = ['url-info-cell', 'header-cell'];

        return `${commonClassNameList.join(' ')} ${className}`;
    }
}
