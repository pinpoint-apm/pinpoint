import { Injectable, ComponentFactoryResolver, Injector } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Subject, Observable, throwError, of } from 'rxjs';
import { map, retry, switchMap } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { UrlQuery, UrlPathId, UrlPath } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService, StoreHelperService, DynamicPopupService, UrlRouteManagerService } from 'app/shared/services';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { isThatType } from 'app/core/utils/util';

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
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
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
            retry(3),
            map(res => res || {}),
            switchMap((res: any) => isThatType(res, 'exception') ? throwError(res) : of(res))
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
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Server Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.move({
                        url: [
                            UrlPath.MAIN
                        ],
                        needServerTimeRequest: false
                    });
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }
    private makeRequestOptionsArgs(to?: number): any {
        return {
            params: new HttpParams()
                .set('applicationName', this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).applicationName)
                .set('serviceTypeName', this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).serviceType)
                .set('from', this.newUrlStateNotificationService.getStartTimeToNumber() + '')
                .set('to', (to || this.newUrlStateNotificationService.getEndTimeToNumber()) + '')
                .set('originTo', this.newUrlStateNotificationService.getEndTimeToNumber() + '')
                .set('calleeRange', this.newUrlStateNotificationService.hasValue(UrlQuery.INBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.INBOUND) : this.webAppSettingDataService.getUserDefaultInbound())
                .set('callerRange', this.newUrlStateNotificationService.hasValue(UrlQuery.OUTBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.OUTBOUND) : this.webAppSettingDataService.getUserDefaultOutbound())
                .set('filter', this.newUrlStateNotificationService.hasValue(UrlQuery.FILTER) ? encodeURIComponent(this.newUrlStateNotificationService.getQueryValue(UrlQuery.FILTER)) : '')
                .set('hint', this.newUrlStateNotificationService.hasValue(UrlQuery.HINT) ? encodeURIComponent(this.newUrlStateNotificationService.getQueryValue(UrlQuery.HINT)) : '')
                .set('v', '4')
                .set('limit', this.REQUEST_LIMIT + '')
                .set('xGroupUnit', this.X_GROUP_UNIT + '') // for scatter-chart
                .set('yGroupUnit', this.Y_GROUP_UNIT + '') // for scatter-chart
        };
    }
}
