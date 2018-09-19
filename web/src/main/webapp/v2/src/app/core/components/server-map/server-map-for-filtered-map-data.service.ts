import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { UrlQuery, UrlPathId } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService, StoreHelperService } from 'app/shared/services';

@Injectable()
export class ServerMapForFilteredMapDataService {
    private url = 'getFilteredServerMapDataMadeOfDotGroup.pinpoint';
    private REQUEST_LIMIT = 5000;
    // 아래 두 값은 scatter-chart에서 사용되는 파라미터 값
    private X_GROUP_UNIT = 987;
    private Y_GROUP_UNIT = 57;
    private requsting = false;
    private flagLoadData = true;
    private nextTo: number;
    private serverMapData = new Subject<any>();
    onServerMapData$: Observable<any>;
    constructor(
        private http: HttpClient,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {
        this.onServerMapData$ = this.serverMapData.asObservable();
    }
    startDataLoad(to?: number): void {
        this.loadData(to);
    }
    stopDataLoad(): void {
        this.flagLoadData = false;
    }
    resumeDataLoad(): void {
        if (this.requsting === false) {
            this.flagLoadData = true;
            this.startDataLoad(this.nextTo);
        }
    }
    private loadData(to?: number): void {
        this.requsting = true;
        this.storeHelperService.dispatch(new Actions.UpdateServerMapLoadingState('loading'));
        this.http.get(this.url, this.makeRequestOptionsArgs(to)).pipe(
            map(res => {
                return res || {};
            })
        ).subscribe((res: any) => {
            if (res['lastFetchedTimestamp'] > res['applicationMapData']['range']['from']) {
                this.nextTo = res['lastFetchedTimestamp'] - 1;
                if (this.flagLoadData) {
                    this.loadData(this.nextTo);
                } else {
                    this.storeHelperService.dispatch(new Actions.UpdateServerMapLoadingState('pause'));
                }
            } else {
                this.storeHelperService.dispatch(new Actions.UpdateServerMapLoadingState('completed'));
            }
            this.serverMapData.next(res);
            this.requsting = false;
        });
    }
    private makeRequestOptionsArgs(to?: number): any {
        return {
            params: {
                applicationName: this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).applicationName,
                serviceTypeName: this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).serviceType,
                from: this.newUrlStateNotificationService.getStartTimeToNumber(),
                to: (to || this.newUrlStateNotificationService.getEndTimeToNumber()),
                originTo: this.newUrlStateNotificationService.getEndTimeToNumber(),
                calleeRange: this.newUrlStateNotificationService.hasValue(UrlQuery.INBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.INBOUND) : this.webAppSettingDataService.getUserDefaultInbound(),
                callerRange: this.newUrlStateNotificationService.hasValue(UrlQuery.OUTBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.OUTBOUND) : this.webAppSettingDataService.getUserDefaultOutbound(),
                filter: this.newUrlStateNotificationService.hasValue(UrlPathId.FILTER) ? encodeURIComponent(this.newUrlStateNotificationService.getPathValue(UrlPathId.FILTER)) : '',
                hint: this.newUrlStateNotificationService.hasValue(UrlPathId.HINT) ? encodeURIComponent(this.newUrlStateNotificationService.getPathValue(UrlPathId.HINT)) : '',
                v: 4,
                limit: this.REQUEST_LIMIT,
                xGroupUnit: this.X_GROUP_UNIT, // for scatter-chart
                yGroupUnit: this.Y_GROUP_UNIT // for scatter-chart
            }
        };
    }
}
