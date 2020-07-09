import { Injectable, ComponentFactoryResolver, Injector } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Subject, Observable, throwError, of, empty, merge } from 'rxjs';
import { retry, switchMap, expand, tap, finalize, takeUntil } from 'rxjs/operators';

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
    private nextTo: number;
    private serverMapData = new Subject<any>();
    onServerMapData$: Observable<any>;

    private isPaused = false;

    private startSrc = new Subject<number>();
    private pauseSrc = new Subject<void>();
    private resumeSrc = new Subject<number>();

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
        this.bindStream();
    }

    startDataLoad(): void {
        this.startSrc.next(this.newUrlStateNotificationService.getEndTimeToNumber());
    }

    stopDataLoad(): void {
        this.isPaused = true;
        this.pauseSrc.next();
        this.storeHelperService.dispatch(new Actions.UpdateServerMapLoadingState('pause'));
    }

    resumeDataLoad(): void {
        this.resumeSrc.next(this.nextTo);
    }

    private bindStream(): void {
        const startObs$ = this.startSrc.asObservable();
        const pauseObs$ = this.pauseSrc.asObservable();
        const resumeObs$ = this.resumeSrc.asObservable();

        const getHttp$ = ((to: number) => this.http.get(this.url, this.makeRequestOptionsArgs(to)).pipe(
            retry(3),
            switchMap((res: any) => isThatType(res, 'exception') ? throwError(res) : of(res))
        ));

        merge(startObs$, resumeObs$).pipe(
            tap(() => this.isPaused = false),
            tap(() => this.storeHelperService.dispatch(new Actions.UpdateServerMapLoadingState('loading'))),
            switchMap((to: number) => {
                return getHttp$(to).pipe(
                    expand(({lastFetchedTimestamp, applicationMapData}) => {
                        this.nextTo = lastFetchedTimestamp - 1;
                        return lastFetchedTimestamp > applicationMapData.range.from ? getHttp$(this.nextTo) : empty();
                    }),
                    takeUntil(pauseObs$),
                    finalize(() => {
                        if (!this.isPaused) {
                            // Should be triggered only when the http call is COMPLETED, not when the http request is CANCELED by the pause button.
                            this.storeHelperService.dispatch(new Actions.UpdateServerMapLoadingState('completed'));
                        }
                    })
                );
            })
        ).subscribe((res: any) => {
            this.serverMapData.next(res);
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
                        needServerTimeRequest: false,
                        queryParams: {
                            filter: null,
                            hint: null,
                        },
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
