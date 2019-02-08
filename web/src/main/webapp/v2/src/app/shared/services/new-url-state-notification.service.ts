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

@Injectable()
export class NewUrlStateNotificationService {
    private startPath: string;
    private urlState: IUrlState = {};
    private innerRouteData: IGeneral = {};
    private onUrlStateChange: BehaviorSubject<NewUrlStateNotificationService> = new BehaviorSubject(null);
    private pageComponentRoute: ActivatedRoute;

    onUrlStateChange$: Observable<NewUrlStateNotificationService>;

    constructor(private componentDefaultSettingDataService: ComponentDefaultSettingDataService) {
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
    updateUrl(startPath: string, pathParams: IGeneral, queryParams: IGeneral, routeData: IGeneral, pageComponentRoute?: ActivatedRoute): void {
        const bStartPathChanged = this.updateStartPath(startPath);
        const bPathChanged = this.updatePathId(pathParams);
        const bQueryChanged = this.updateQuery(queryParams);
        this.pageComponentRoute = pageComponentRoute;
        this.updateRouteData(routeData);

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
    private updatePathId(pathParams: IGeneral): boolean {
        let updated = false;

        UrlPathId.getPathIdList().forEach((path: string) => {
            this.changePathIdState(path, pathParams[path] ? UrlPathIdFactory.createPath(path, pathParams[path]) : null);
            if (this.isPathChanged(path)) {
                updated = true;
            }
        });

        if (this.isPathChanged(UrlPathId.FOCUS_TIMESTAMP)) {
            this.setConnectedPath(pathParams[UrlPathId.FOCUS_TIMESTAMP]);
        }

        return updated;
    }
    isPathChanged(path: string): boolean {
        const { prev, curr } = this.urlState[path];

        return prev === null ? curr !== null : !prev.equals(curr);
    }
    private changePathIdState(path: string, newPathIdObject: IUrlPathId<any>): void {
        this.urlState[path].prev = this.urlState[path].curr;
        this.urlState[path].curr = newPathIdObject;
    }
    private setConnectedPath(focusTimeStamp: string): void {
        this.urlState[UrlPathId.END_TIME].prev = this.urlState[UrlPathId.END_TIME].curr;
        this.urlState[UrlPathId.END_TIME].curr = UrlPathIdFactory.createPath(UrlPathId.END_TIME, EndTime.formatDate((Number(focusTimeStamp) + (1000 * 60 * 10))));
        this.urlState[UrlPathId.PERIOD].prev = this.urlState[UrlPathId.PERIOD].curr;
        this.urlState[UrlPathId.PERIOD].curr = UrlPathIdFactory.createPath(UrlPathId.PERIOD, this.componentDefaultSettingDataService.getSystemDefaultTransactionViewPeriod().getValueWithTime());
    }
    private updateQuery(queryParams: IGeneral): boolean {
        let updated = false;
        UrlQuery.getQueryList().forEach((query: string) => {
            if (this.changedQuery(query, queryParams[query])) {
                this.changeQueryState(query, queryParams[query] ? UrlQueryFactory.createQuery(query, queryParams[query]) : null);
                updated = true;
            }
        });
        return updated;
    }
    private changedQuery(query: string, queryValue: any): boolean {
        if (queryValue === null || queryValue === undefined) {
            return this.urlState[query].curr !== null;
        } else {
            if (this.urlState[query].curr === null) {
                return true;
            } else {
                return !(this.urlState[query].curr as IUrlQuery<any>).equals(UrlQueryFactory.createQuery(query, queryValue));
            }
        }
    }
    private changeQueryState(query: string, newQueryObject: IUrlQuery<any>): void {
        this.urlState[query].prev = this.urlState[query].curr;
        this.urlState[query].curr = newQueryObject;
    }
    private updateRouteData(routeData: IGeneral) {
        Object.assign(this.innerRouteData, routeData);
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
        return names.reduce((previous: boolean, name: string) => {
            return previous && (this.urlState[name].curr === null ? false : true);
        }, true);
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
}
