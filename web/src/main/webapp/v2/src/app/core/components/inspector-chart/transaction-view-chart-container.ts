import { ChangeDetectorRef, ElementRef } from '@angular/core';
import { Subject, Observable, combineLatest } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';

import { II18nText, IChartConfig, IErrObj } from 'app/core/components/inspector-chart/inspector-chart.component';
import { StoreHelperService, WebAppSettingDataService, NewUrlStateNotificationService, AjaxExceptionCheckerService, GutterEventService } from 'app/shared/services';
import { IChartDataService, IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';
import { UrlPathId } from 'app/shared/models';

export abstract class TransactionViewChartContainer {
    protected chartData: IChartDataFromServer;
    protected timezone: string;
    protected dateFormat: string[];
    protected unsubscribe = new Subject<void>();

    i18nText$: Observable<II18nText>;
    height$: Observable<string>;
    chartConfig: IChartConfig;
    errObj: IErrObj;
    hoveredInfo$: Observable<IHoveredInfo>;
    xRawData: number[];

    constructor(
        protected defaultYMax: number,
        protected storeHelperService: StoreHelperService,
        protected changeDetector: ChangeDetectorRef,
        protected webAppSettingDataService: WebAppSettingDataService,
        protected newUrlStateNotificationService: NewUrlStateNotificationService,
        protected chartDataService: IChartDataService,
        protected translateService: TranslateService,
        protected ajaxExceptionCheckerService: AjaxExceptionCheckerService,
        protected gutterEventService: GutterEventService,
        protected el: ElementRef
    ) {}

    protected initI18nText(): void {
        this.i18nText$ = combineLatest(
            this.translateService.get('COMMON.FAILED_TO_FETCH_DATA'),
            this.translateService.get('INSPECTOR.NO_DATA_COLLECTED'),
        ).pipe(
            map(([FAILED_TO_FETCH_DATA, NO_DATA_COLLECTED]: string[]) => {
                return { FAILED_TO_FETCH_DATA, NO_DATA_COLLECTED };
            })
        );
    }
    protected initHoveredInfo(): void {
        this.hoveredInfo$ = this.storeHelperService.getHoverInfo(this.unsubscribe).pipe(
            filter(() => {
                return !(!this.chartConfig || this.chartConfig.isDataEmpty);
            })
        );
    }
    protected initTimezoneAndDateFormat(): void {
        combineLatest(
            this.storeHelperService.getTimezone(this.unsubscribe),
            this.storeHelperService.getDateFormatArray(this.unsubscribe, 3, 4)
        ).subscribe((data: [string, string[]]) => {
            this.timezone = data[0];
            this.dateFormat = data[1];
            if (this.chartData) {
                const xDataArr = Array.isArray(this.chartData) ? this.chartData[0].charts.x : this.chartData.charts.x;

                this.chartConfig = {...this.chartConfig};
                this.chartConfig.dataConfig.labels = this.getNewFormattedLabels(xDataArr);
                this.changeDetector.detectChanges();
            }
        });
    }

    protected initHeight(): void {
        // TODO: angular-split라이브러리에 minSize옵션 추가되면, filter오퍼레이터 제거.
        this.height$ = this.gutterEventService.onGutterResized$.pipe(
            map((ratioArr: number[]) => ratioArr[0]),
            filter((ratio: number) => ratio >= 30 && ratio <= 55), // 30, 50: 차트가 포함되어 있는 split-area의 최소, 최대 사이즈(비율)
            map(() => this.el.nativeElement.offsetHeight + 'px')
        );
    }

    private getNewFormattedLabels(xDataArr: number[]): string[] {
        return xDataArr.map((xData: number) => {
            return `${moment(xData).tz(this.timezone).format(this.dateFormat[0])}#${moment(xData).tz(this.timezone).format(this.dateFormat[1])}`;
        });
    }

    protected getTimeRange(): number[] {
        const focusTime = Number(this.newUrlStateNotificationService.getPathValue(UrlPathId.FOCUS_TIMESTAMP));
        const range = 600000;

        return [focusTime - range, focusTime + range];
    }

    onRetryGetChartData(): void {
        this.getChartData(this.getTimeRange());
    }

    protected getChartData(range: number[]): void {
        this.chartDataService.getData(range)
            .subscribe(
                (data: IChartDataFromServer | AjaxException) => {
                    if (this.ajaxExceptionCheckerService.isAjaxException(data)) {
                        this.setErrObj(data);
                    } else {
                        this.xRawData = data.charts.x;
                        this.chartData = data;
                        this.setChartConfig(this.makeChartData(data));
                    }
                },
                (err) => {
                    this.setErrObj();
                }
            );
    }

    protected setChartConfig(data: {[key: string]: any} | {[key: string]: any}[]): void {
        this.chartConfig =  {
            type: 'line',
            dataConfig: this.makeDataOption(data),
            elseConfig: this.makeNormalOption(data),
            isDataEmpty: this.isDataEmpty(data)
        };
        this.changeDetector.detectChanges();
    }

    protected setErrObj(data?: AjaxException): void {
        this.errObj = {
            errType: data ? 'EXCEPTION' : 'ELSE',
            errMessage: data ? data.exception.message : null
        };
        this.changeDetector.detectChanges();
    }

    protected isDataEmpty(data: {[key: string]: any} | {[key: string]: any}[]): boolean {
        const emptyCheckFunc = (d: {[key: string]: any}) => Object.getOwnPropertyNames(d).filter((prop) => prop !== 'x' && Array.isArray(d[prop])).map((yProp) => d[yProp].length).every((l) => l === 0);

        return Array.isArray(data) ? data.length === 0 || data.every((obj) => emptyCheckFunc(obj))
            : emptyCheckFunc(data);
    }

    protected parseData(data: number): number | null {
        return data === -1 ? null : data;
    }

    protected abstract makeChartData(chartData: IChartDataFromServer): {[key: string]: any} | {[key: string]: any}[];
    protected abstract makeDataOption(data: {[key: string]: any} | {[key: string]: any}[]): {[key: string]: any};
    protected abstract makeNormalOption(data: {[key: string]: any} | {[key: string]: any}[]): {[key: string]: any};
}
