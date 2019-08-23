import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { pluck } from 'rxjs/operators';

import { ComponentDefaultSettingDataService } from 'app/shared/services/component-default-setting-data.service';
import { EndTime } from 'app/core/models/end-time';
import { UrlPath, UrlPathIdFactory, UrlPathId, IUrlPathId, UrlQueryFactory, UrlQuery, IUrlQuery } from 'app/shared/models';

interface IGeneral {
    [key: string]: any;
}

interface IUrlState {
    [key: string]: {
        prev: IUrlPathId<any> | IUrlQuery<any>;
        curr: IUrlPathId<any> | IUrlQuery<any>;
    };
}

export interface IUrlInfo {
    startPath: string;
    pathParams: Map<string, string>;
    queryParams: Map<string, string>;
}

@Injectable()
export class NewUrlStateNotificationService {
    private startPath: string;
    private urlState: IUrlState = {};
    private innerRouteData: IGeneral = {};
    private onUrlStateChange: BehaviorSubject<NewUrlStateNotificationService> = new BehaviorSubject(null);
    private pageComponentRoute: ActivatedRoute;
    private pageUrlHistory: IUrlInfo[] = [];

    onUrlStateChange$: Observable<NewUrlStateNotificationService>;

    constructor(
        private componentDefaultSettingDataService: ComponentDefaultSettingDataService
    ) {
        this.onUrlStateChange$ = this.onUrlStateChange.asObservable();
        this.initState();
    }
    private initState(): void {
        UrlPathId.getPathIdList().forEach((path: string) => {
            this.urlState[path] = {
                prev: null,
                curr: null
            };
        });
        UrlQuery.getQueryList().forEach((query: string) => {
            this.urlState[query] = {
                prev: null,
                curr: null
            };
        });
    }
    updateUrl(startPath: string, pathParams: Map<string, string>, queryParams: Map<string, string>, routeData: IGeneral, pageComponentRoute: ActivatedRoute): void {
        const bStartPathChanged = this.updateStartPath(startPath);
        const bPathChanged = this.updatePathId(pathParams);
        const bQueryChanged = this.updateQuery(queryParams);
        this.pageComponentRoute = pageComponentRoute;
        this.updateRouteData(routeData);

        if (bStartPathChanged || (startPath !== UrlPath.CONFIG && (bPathChanged || bQueryChanged))) {
            const newUrlInfo = { startPath, pathParams, queryParams };

            this.pageUrlHistory = this.pageUrlHistory.length < 2 ? [ ...this.pageUrlHistory, newUrlInfo ] : [ this.pageUrlHistory[1], newUrlInfo ];
        }

        if (bStartPathChanged || bPathChanged || bQueryChanged) {
            // this.onUrlStateChange.next(this.urlState);
            this.onUrlStateChange.next(this);
        }
    }
    private updateStartPath(path: string): boolean {
        if (this.startPath === path) {
            return false;
        } else {
            this.startPath = path;
            return true;
        }
    }
    private updatePathId(pathParams: Map<string, string>): boolean {
        let updated = false;

        UrlPathId.getPathIdList().forEach((path: string) => {
            const pathValue = pathParams.get(path);

            this.updateUrlState(path, pathValue ? UrlPathIdFactory.createPath(path, pathValue) : null);
            if (this.isValueChanged(path)) {
                updated = true;
            }
        });

        if (this.isValueChanged(UrlPathId.FOCUS_TIMESTAMP)) {
            this.setConnectedPath(pathParams.get(UrlPathId.FOCUS_TIMESTAMP));
        }

        return updated;
    }
    isValueChanged(key: string): boolean {
        const { prev, curr } = this.urlState[key];

        return prev === null ? curr !== null : !prev.equals(curr);
    }
    private updateUrlState(key: string, newStateObj: IUrlPathId<any> | IUrlQuery<any>): void {
        this.urlState[key].prev = this.urlState[key].curr;
        this.urlState[key].curr = newStateObj;
    }
    private setConnectedPath(focusTimeStamp: string): void {
        this.urlState[UrlPathId.END_TIME].prev = this.urlState[UrlPathId.END_TIME].curr;
        this.urlState[UrlPathId.END_TIME].curr = UrlPathIdFactory.createPath(UrlPathId.END_TIME, EndTime.formatDate((Number(focusTimeStamp) + (1000 * 60 * 10))));
        this.urlState[UrlPathId.PERIOD].prev = this.urlState[UrlPathId.PERIOD].curr;
        this.urlState[UrlPathId.PERIOD].curr = UrlPathIdFactory.createPath(UrlPathId.PERIOD, this.componentDefaultSettingDataService.getSystemDefaultTransactionViewPeriod().getValueWithTime());
    }
    private updateQuery(queryParams: Map<string, string>): boolean {
        let updated = false;

        UrlQuery.getQueryList().forEach((query: string) => {
            const queryValue = queryParams.get(query);

            this.updateUrlState(query, queryValue ? UrlQueryFactory.createQuery(query, queryValue) : null);
            if (this.isValueChanged(query)) {
                updated = true;
            }
        });

        return updated;
    }
    private updateRouteData(routeData: IGeneral) {
        this.innerRouteData = { ...this.innerRouteData, ...routeData };
    }
    getConfiguration(key: string): Observable<any> {
        return this.pageComponentRoute.data.pipe(
            pluck('configuration', key)
        );
    }
    isRealTimeMode(type?: string): boolean {
        if (typeof type === 'string') {
            return type === UrlPath.REAL_TIME;
        } else {
            return this.innerRouteData['enableRealTimeMode'] || false;
        }
    }
    showRealTimeButton(): boolean {
        return this.innerRouteData['showRealTimeButton'] || false;
    }
    getUrlServerTimeData(): number {
        return this.innerRouteData['serverTime'];
    }
    hasValue(...names: string[]): boolean {
        return names.every((name: string) => this.urlState[name].curr !== null);
    }
    getStartPath(): string {
        return this.startPath || UrlPath.MAIN;
    }
    getPathValue(path: string): any {
        return this.urlState[path].curr && this.urlState[path].curr.get();
    }
    getQueryValue(query: string): any {
        return this.urlState[query].curr && this.urlState[query].curr.get();
    }
    getStartTimeToNumber(): number {
        if (this.isRealTimeMode()) {
            return this.getUrlServerTimeData() - (this.componentDefaultSettingDataService.getSystemDefaultPeriod().getMiliSeconds());
        } else if (this.getPathValue(UrlPathId.END_TIME) && this.getPathValue(UrlPathId.PERIOD)) {
            return this.getPathValue(UrlPathId.END_TIME).calcuStartTime(this.getPathValue(UrlPathId.PERIOD).getValue()).getDate().valueOf();
        }
        return Date.now();
    }
    getEndTimeToNumber(): number {
        if (this.isRealTimeMode()) {
            return this.getUrlServerTimeData();
        } else if (this.getPathValue(UrlPathId.END_TIME)) {
            return this.getPathValue(UrlPathId.END_TIME).getDate().valueOf();
        }
        return Date.now();
    }
    getPrevPageUrlInfo(): IUrlInfo {
        return this.pageUrlHistory[0];
    }
}
