import { Injectable, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { APP_BASE_HREF } from '@angular/common';

import { WindowRefService } from 'app/shared/services/window-ref.service';
import { ServerTimeDataService } from 'app/shared/services/server-time-data.service';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services/new-url-state-notification.service';
import { WebAppSettingDataService } from 'app/shared/services/web-app-setting-data.service';
import { isEmpty } from 'app/core/utils/util';
import { Period, EndTime } from 'app/core/models';

// TODO: Router Navigation Refactoring
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
        const startPath = this.newUrlStateNotificationService.getStartPath();
        const realTimePath = UrlPath.REAL_TIME;
        const applicationPath = applicationUrlStr || this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr();
        const baseUrl = [startPath, realTimePath, applicationPath];
        const finalUrl = this.newUrlStateNotificationService.hasValue(UrlPathId.AGENT_ID) ? [...baseUrl, this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID)] : baseUrl;

        this.router.navigate(finalUrl, {
            queryParamsHandling: 'preserve'
        });
    }

    moveToConfigPage(type: string): void {
        this.router.navigate([
            UrlPath.CONFIG,
            type
        ]);
    }

    moveToMain(): void {
        this.router.navigate([
            UrlPath.MAIN
        ]);
    }

    moveToAppMenu(rootRoute: string): void {
        /**
         * Navigation flow
         * 1. From HostMenu to AppMenu: Navigate to the last url state
         * 2. From AppMenu to AppMenu: Keep the every state(app, period, endTime)
         * 3. From AppMenu to AppMenu in realtime: Keep the app state with the default period, endTime
         */
        const urlService = this.newUrlStateNotificationService;
        const pageUrlInfo = urlService.getPageUrlInfo(rootRoute);

        if (urlService.hasValue(UrlPathId.APPLICATION)) {
            const isRealTimeMode = urlService.isRealTimeMode()
            const app = urlService.getPathValue(UrlPathId.APPLICATION) as IApplication;
            const period = urlService.getPathValue(UrlPathId.PERIOD) as Period;
            const endTime = urlService.getPathValue(UrlPathId.END_TIME) as EndTime;

            if (isRealTimeMode) {
                this.router.navigate([
                    rootRoute,
                    app.getUrlStr()
                ]);    
            } else {
                this.router.navigate([
                    rootRoute,
                    app.getUrlStr(),
                    period.getValueWithTime(),
                    endTime.getEndTime()
                ]);
            }
        } else if (pageUrlInfo) {
            this.router.navigate([
                rootRoute,
                ...pageUrlInfo.pathParams.values()
            ]);
        } else {
            this.router.navigate([
                rootRoute
            ]);
        }
    }

    moveToHostMenu(rootRoute: string): void {
        const urlService = this.newUrlStateNotificationService;
        const pageUrlInfo = urlService.getPageUrlInfo(rootRoute);

        if (!pageUrlInfo) {
            this.router.navigate([
                rootRoute
            ]);

            return;
        }

        this.router.navigate([
            rootRoute,
            ...pageUrlInfo.pathParams.values()
        ], {
            queryParams: Object.fromEntries(pageUrlInfo.queryParams),
        });
    }

    move({url, needServerTimeRequest, nextUrl = [], queryParams = {}}: {url: string[], needServerTimeRequest: boolean, nextUrl?: string[], queryParams?: any}): void {
        url = url[0] === this.getBaseHref().replace(/\//g, '') ? url.slice(1) : url;

        const query = Object.entries(queryParams).reduce((acc: {[key: string]: any}, [key, value]: [string, any]) => {
            const queryValue = (typeof value === 'object' && value !== null) ? JSON.stringify(value) : value;

            return {...acc, [key]: queryValue};
        }, {});

        if (needServerTimeRequest) {
            this.serverTimeDataService.getServerTime().subscribe(time => {
                const newUrl = url.concat([EndTime.formatDate(time)]).concat(nextUrl).filter((v: string) => {
                    return v !== '';
                });

                if (!isEmpty(queryParams)) {
                    this.router.navigate(newUrl, {
                        queryParams: query,
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

            if (!isEmpty(queryParams)) {
                this.router.navigate(newUrl, {
                    queryParams: query,
                    queryParamsHandling: 'merge'
                });
            } else {
                this.router.navigate(newUrl, {
                    queryParamsHandling: 'preserve'
                });
            }
        }
    }

    moveOnPage({url, queryParams}: {url: string[], queryParams?: any}): void {
        this.move({
            url,
            needServerTimeRequest: false,
            nextUrl: [],
            queryParams
        });
    }

    openPage({path, queryParams = {}, metaInfo = ''}: {path: string[], queryParams?: {[key: string]: any}, metaInfo?: string}): any {
        const pathStr = path.filter((p: string) => !!p).join('/');
        const queryStr = Object.entries(queryParams).map(([key, value]: [string, any]) => {
            const stringifyValue = (typeof value === 'object' && value !== null) ? JSON.stringify(value) : value;

            return `${key}=${encodeURIComponent(stringifyValue)}`;
        }).join('&');

        return this.windowRef.nativeWindow.open(`${this.getBaseHref()}${pathStr}${queryStr ? `?${queryStr}` : ''}`, metaInfo);
    }

    openInspectorPage(isRealTimeMode: boolean, selectedApp: string, selectedAgent: string): void {
        isRealTimeMode ?
            this.openPage({
                path: [
                    UrlPath.INSPECTOR,
                    UrlPath.REAL_TIME,
                    selectedApp,
                    selectedAgent
                ]
            }) :
            this.openPage({
                path: [
                    UrlPath.INSPECTOR,
                    selectedApp,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                    selectedAgent
                ]
            });
    }

    reload(): void {
        this.windowRef.nativeWindow.location.reload();
    }

    back(): void {
        this.windowRef.nativeWindow.history.back();
    }

    getBaseHref(): string {
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
