import { Injectable, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { WindowRefService } from 'app/shared/services/window-ref.service';
import { ServerTimeDataService } from 'app/shared/services/server-time-data.service';
import { FilterParamMaker } from 'app/core/utils/filter-param-maker';
import { HintParamMaker } from 'app/core/utils/hint-param-maker';
import { EndTime } from 'app/core/models/end-time';
import { Filter } from 'app/core/models/filter';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services/new-url-state-notification.service';
import { WebAppSettingDataService } from 'app/shared/services/web-app-setting-data.service';
import { APP_BASE_HREF } from '@angular/common';

@Injectable()
export class UrlRouteManagerService {

    constructor(
        private windowRef: WindowRefService,
        private router: Router,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverTimeDataService: ServerTimeDataService,
        @Inject(APP_BASE_HREF) private baseHref: string
    ) {}
    changeApplication(applicationUrlStr: string): void {
        const startPath = this.newUrlStateNotificationService.getStartPath();
        if (this.newUrlStateNotificationService.isRealTimeMode()) {
            this.moveToRealTime(applicationUrlStr);
        } else {
            this.serverTimeDataService.getServerTime().subscribe(time => {
                const url = [
                    startPath,
                    applicationUrlStr,
                    this.webAppSettingDataService.getUserDefaultPeriod().getValueWithTime(),
                    EndTime.formatDate(time)
                ];
                this.router.navigate(url, {
                    queryParamsHandling: 'preserve'
                });
            });
        }
    }
    moveToRealTime(applicationUrlStr?: string): void {
        this.router.navigate([
            this.newUrlStateNotificationService.getStartPath(),
            (applicationUrlStr || this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr()),
            UrlPath.REAL_TIME
        ]);
    }
    moveToConfigPage(type: string): void {
        this.router.navigate([
            UrlPath.CONFIG,
            type
        ]);
    }
    move({ url, needServerTimeRequest, nextUrl = [], queryParam }: { url: string[], needServerTimeRequest: boolean, nextUrl?: string[], queryParam?: any} ): void {
        if (needServerTimeRequest) {
            this.serverTimeDataService.getServerTime().subscribe(time => {
                const newUrl = url.concat([EndTime.formatDate(time)]).concat(nextUrl).filter((v: string) => {
                    return v !== '';
                });
                if (queryParam) {
                    this.router.navigate(newUrl, {
                        queryParams: queryParam,
                        queryParamsHandling: 'merge'
                    });
                } else {
                    this.router.navigate(newUrl, {
                        queryParamsHandling: 'preserve'
                    });
                }
            });
        } else {
            const newUrl = [...url, ...nextUrl];

            if (queryParam) {
                this.router.navigate(newUrl, {
                    queryParams: queryParam,
                    queryParamsHandling: 'merge'
                });
            } else {
                this.router.navigate(newUrl, {
                    queryParamsHandling: 'preserve'
                });
            }
        }
    }
    moveOnPage({ url, queryParam }: { url: string[], queryParam?: any }): void {
        this.move({
            url: url,
            needServerTimeRequest: false,
            nextUrl: [],
            queryParam: queryParam
        });
    }
    openPage(path: string | string[], title?: string): void {
        this.windowRef.nativeWindow.open(this.getBaseHref() + (path instanceof Array ? path.join('/') : path), title || '');
    }
    makeFilterMapUrl(
        { applicationName, serviceType, periodStr, timeStr, filterStr, hintStr, addedFilter, addedHint }:
        { applicationName: string, serviceType: string, periodStr: string, timeStr: string, filterStr: string, hintStr: string, addedFilter: Filter, addedHint?: any }
    ): string {
        return `filteredMap/${applicationName}@${serviceType}/${periodStr}/${timeStr}` +
            FilterParamMaker.makeParam(filterStr, addedFilter) +
            HintParamMaker.makeParam(hintStr, addedHint);
    }
    openInspectorPage(realTimeMode: boolean): void {
        if (realTimeMode) {
            this.serverTimeDataService.getServerTime().subscribe(time => {
                this.windowRef.nativeWindow.open([
                    this.getBaseHref() + UrlPath.INSPECTOR,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                    this.webAppSettingDataService.getSystemDefaultPeriod().getValueWithTime(),
                    EndTime.newByNumber(time).getEndTime()
                ].join('/'));
            });
        } else {
            this.windowRef.nativeWindow.open([
                this.getBaseHref() + UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
            ].join('/'));
        }
    }
    openMainPage(): void {
        this.windowRef.nativeWindow.open([
            this.getBaseHref() + UrlPath.MAIN,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
        ].join('/'));
    }
    reload(): void {
        this.windowRef.nativeWindow.location.reload();
    }
    back(): void {
        this.windowRef.nativeWindow.history.back();
    }
    private getBaseHref(): string {
        if (this.baseHref === '/') {
            return '';
        } else {
            if (/.*\/$/.test(this.baseHref)) {
                return this.baseHref;
            } else {
                return this.baseHref + '/';
            }
        }
    }
}
