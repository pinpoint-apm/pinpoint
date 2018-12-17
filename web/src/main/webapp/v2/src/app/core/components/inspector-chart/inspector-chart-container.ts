import { ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';
import { Subject, Observable, combineLatest, merge } from 'rxjs';
import { filter, map, skip, takeUntil, withLatestFrom } from 'rxjs/operators';

import { II18nText, IChartConfig, IErrObj } from 'app/core/components/inspector-chart/inspector-chart.component';
import { WebAppSettingDataService, NewUrlStateNotificationService, AjaxExceptionCheckerService, AnalyticsService, TRACKED_EVENT_LIST, StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { IChartDataService, IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';

export abstract class InspectorChartContainer {
    private previousRange: number[];

    protected chartData: IChartDataFromServer | IChartDataFromServer[];
    protected timezone: string;
    protected dateFormat: string[];
    protected unsubscribe = new Subject<void>();

    i18nText$: Observable<II18nText>;
    chartConfig: IChartConfig;
    errObj: IErrObj;
    hoveredInfo$: Observable<IHoveredInfo>;

    constructor(
        protected defaultYMax: number,
        protected storeHelperService: StoreHelperService,
        protected changeDetector: ChangeDetectorRef,
        protected webAppSettingDataService: WebAppSettingDataService,
        protected newUrlStateNotificationService: NewUrlStateNotificationService,
        protected chartDataService: IChartDataService,
        protected translateService: TranslateService,
        protected ajaxExceptionCheckerService: AjaxExceptionCheckerService,
        protected analyticsService: AnalyticsService,
        protected dynamicPopupService: DynamicPopupService
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
            skip(1),
            filter(() => {
                return !(!this.chartConfig || this.chartConfig.isDataEmpty);
            })
        );
    }

    protected initTimezoneAndDateFormat(): void {
        combineLatest(
            this.storeHelperService.getTimezone(this.unsubscribe),
            this.storeHelperService.getDateFormatArray(this.unsubscribe, 3, 4)
        ).subscribe(([timezone, dateFormat]: [string, string[]]) => {
            this.timezone = timezone;
            this.dateFormat = dateFormat;
            if (this.chartData) {
                const xDataArr = Array.isArray(this.chartData) ? this.chartData[0].charts.x : this.chartData.charts.x;

                this.chartConfig = {...this.chartConfig};
                this.chartConfig.dataConfig.labels = this.getNewFormattedLabels(xDataArr);
                this.changeDetector.detectChanges();
            }
        });
    }

    private getNewFormattedLabels(xDataArr: number[]): string[] {
        return xDataArr.map((xData: number) => {
            return `${moment(xData).tz(this.timezone).format(this.dateFormat[0])}#${moment(xData).tz(this.timezone).format(this.dateFormat[1])}`;
        });
    }

    protected initChartData(): void {
        merge(
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                takeUntil(this.unsubscribe),
                withLatestFrom(this.storeHelperService.getInspectorTimelineSelectionRange(this.unsubscribe)),
                map(([, storeState]: [NewUrlStateNotificationService, number[]]) => storeState),
            ),
            this.storeHelperService.getInspectorTimelineSelectionRange(this.unsubscribe).pipe(
                skip(1),
                withLatestFrom(this.newUrlStateNotificationService.onUrlStateChange$),
                filter(([storeState, urlService]: [number[], NewUrlStateNotificationService]) => {
                    const [from, to] = storeState;

                    return !(from === urlService.getStartTimeToNumber() && to === urlService.getEndTimeToNumber());
                }),
                map(([storeState]: [number[], NewUrlStateNotificationService]) => storeState),
                filter((storeState: number[]) => {
                    const [f0, t0] = storeState;
                    const [f1, t1] = this.previousRange;

                    return !(f0 === f1 && t0 === t1);
                }),
            )
        ).subscribe((range: number[]) => {
            this.previousRange = range;
            this.getChartData(range);
        });
    }

    onRetryGetChartData(): void {
        this.getChartData(this.previousRange);
    }

    protected getChartData(range: number[]): void {
        this.chartDataService.getData(range).subscribe((data: IChartDataFromServer | IChartDataFromServer[] | AjaxException) => {
            if (this.ajaxExceptionCheckerService.isAjaxException(data)) {
                this.setErrObj(data);
            } else {
                this.chartData = data;
                this.setChartConfig(this.makeChartData(data));
            }
        }, () => {
            this.setErrObj();
        });
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

    protected abstract makeChartData(chartData: IChartDataFromServer | IChartDataFromServer[]): {[key: string]: any} | {[key: string]: any}[];
    protected abstract makeDataOption(data: {[key: string]: any} | {[key: string]: any}[]): {[key: string]: any};
    protected abstract makeNormalOption(data: {[key: string]: any} | {[key: string]: any}[]): {[key: string]: any};
    onShowHelp($event: MouseEvent, key: HELP_VIEWER_LIST): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, key);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: key,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        });
    }
}
