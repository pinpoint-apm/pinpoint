import { catchError } from 'rxjs/operators';
import { Injectable, ComponentFactoryResolver, Injector } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Subject, Observable, EMPTY } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { UrlPath, UrlPathId, UrlQuery } from 'app/shared/models';
import {
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    WindowRefService,
    DynamicPopupService,
    WebAppSettingDataService
} from 'app/shared/services';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Injectable()
export class TransactionMetaDataService {
    private requestURLV1 = 'transactionmetadata.pinpoint';
    private requestURLV2 = 'heatmap/drag.pinpoint';
    private retrieveErrorMessage: string;
    private lastFetchedFrom: number;
    private lastFetchedIndex = 0;
    private maxLoadLength = 100;
    private requestSourceData: any[];
    private requestCount = 0;
    private countStatus = [0, 0];
    private enableServerSideScan: boolean;
    private outTransactionDataLoad = new Subject<ITransactionMetaData[]>();
    private outTransactionDataRange = new Subject<number[]>();
    private outTransactionDataCount = new Subject<number[]>();
    private outTransactionDataFetchState = new Subject<boolean>();

    onTransactionDataLoad$: Observable<ITransactionMetaData[]>;
    onTransactionDataRange$: Observable<number[]>;
    onTransactionDataCount$: Observable<number[]>;
    onTransactionDataFecthState$: Observable<boolean>;

    constructor(
        private http: HttpClient,
        private windowRefService: WindowRefService,
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private dynamicPopupService: DynamicPopupService,
        private webAppSettingDataService: WebAppSettingDataService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {
        this.onTransactionDataLoad$ = this.outTransactionDataLoad.asObservable();
        this.onTransactionDataRange$ = this.outTransactionDataRange.asObservable();
        this.onTransactionDataCount$ = this.outTransactionDataCount.asObservable();
        this.onTransactionDataFecthState$ = this.outTransactionDataFetchState.asObservable();

        this.translateService.get('TRANSACTION_LIST.TRANSACTION_RETRIEVE_ERROR').subscribe((text: string) => {
            this.retrieveErrorMessage = text;
        });
    }
    loadData(): void {
        this.webAppSettingDataService.getExperimentalConfiguration().subscribe(configuration => {
            // * Check withFilter Param in order to disable the server-side scanning temporarily
            const withFilter = this.newUrlStateNotificationService.hasValue(UrlQuery.WITH_FILTER);
            const enableServerSideScan = this.webAppSettingDataService.getExperimentalOption('scatterScan');

            this.enableServerSideScan = withFilter ? false
                : enableServerSideScan === null ? configuration.enableServerSideScanForScatter.value : enableServerSideScan;
        });

        if (this.enableServerSideScan) {
            this.http.get<{metadata: ITransactionMetaData[], resultFrom: number, complete: boolean}>(this.requestURLV2, this.makeV2RequestOptionsArgs())
                .pipe(
                    catchError((error: IServerError) => {
                        this.onError({
                            data: {title: 'Error', contents: error},
                            component: ServerErrorPopupContainerComponent
                        });

                        return EMPTY;
                    })
                )
                .subscribe((responseData: {metadata: ITransactionMetaData[], resultFrom: number, complete: boolean}) => {
                    const responseLength = responseData.metadata.length;

                    if (responseLength === 0) {
                        this.outFullRange();
                        this.outTransactionDataFetchState.next(true);
                    } else {
                        if (responseData.complete) {
                            this.lastFetchedFrom = null;
                            this.outFullRange();
                            this.outTransactionDataFetchState.next(true);
                        } else {
                            this.lastFetchedFrom = responseData.resultFrom;
                            this.outTransactionDataRange.next([
                                responseData.metadata[responseLength - 1].collectorAcceptTime,
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getDate().valueOf()
                            ]);
                            this.outTransactionDataFetchState.next(false);
                        }

                    }
                    this.outTransactionDataLoad.next(responseData.metadata);
                });
        } else {
            this.requestSourceData = this.getInfoFromOpener();
            this.countStatus[1] = this.requestSourceData.length;

            if (this.requestSourceData.length === 0) {
                this.onError({
                    data: {title: 'Notice', contents: this.retrieveErrorMessage, type: 'html'},
                    component: MessagePopupContainerComponent,
                });
            } else {
                this.http.post<{metadata: ITransactionMetaData[]}>(this.requestURLV1, this.makeV1RequestOptionsArgs(), {
                    headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
                }).pipe(
                    catchError((error: IServerError) => {
                        this.onError({
                            data: {title: 'Error', contents: error},
                            component: ServerErrorPopupContainerComponent
                        });

                        return EMPTY;
                    })
                ).subscribe((responseData: {metadata: ITransactionMetaData[]}) => {
                    const responseLength = responseData.metadata.length;
                    if (this.requestCount !== responseLength) {
                        if (this.requestCount > responseLength) {
                            this.countStatus[1] -= (this.requestCount - responseLength);
                        } else {
                            this.countStatus[1] += (responseLength - this.requestCount);
                        }
                    }
                    if (responseLength === 0) {
                        this.outFullRange();
                    } else {
                        this.countStatus[0] += responseLength;
                        if (this.countStatus[0] === this.countStatus[1]) {
                            this.outFullRange();
                        } else {
                            this.outTransactionDataRange.next([
                                responseData.metadata[responseLength - 1].collectorAcceptTime,
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getDate().valueOf()
                            ]);
                        }
                        // this.outTransactionDataLoad.next(responseData.metadata);
                    }

                    this.outTransactionDataLoad.next(responseData.metadata);
                    this.outTransactionDataCount.next(this.countStatus);
                });
            }
        }
    }
    moreLoad(): void {
        this.loadData();
    }
    private outFullRange(): void {
        this.outTransactionDataRange.next([
            this.newUrlStateNotificationService.getStartTimeToNumber(),
            this.newUrlStateNotificationService.getEndTimeToNumber()
        ]);
    }
    private makeV2RequestOptionsArgs(): {[key: string]: any} {
        const {x1, x2, y1, y2, agentId, dotStatus} = JSON.parse(this.newUrlStateNotificationService.getQueryValue(UrlQuery.DRAG_INFO));
        const application = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName();

        let params = new HttpParams()
            .set('application', application)
            .set('x1', x1)
            .set('x2', this.lastFetchedFrom ? this.lastFetchedFrom - 1 : x2)
            .set('y1', y1)
            .set('y2', y2);

        if (agentId) {
            params = params.set('agentId', agentId);
        }

        // TODO: dotStatus 파라미터 전달 방식 및 조건처리 고민
        if (dotStatus.length === 1) {
            params = params.set('dotStatus', (dotStatus[0] === 'success').toString());
        }

        return {params};
    }
    private makeV1RequestOptionsArgs(): string {
        const requestStr = [];
        const len = this.requestSourceData.length;
        const appName = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
        const maxLen = Math.min(len, this.maxLoadLength + this.lastFetchedIndex);

        for (let index = 0, dataIndex = this.lastFetchedIndex; dataIndex < maxLen; index++, dataIndex++) {
            // transactionId, x, y
            requestStr.push(`I${index}=${this.requestSourceData[dataIndex][0]}`);
            requestStr.push(`T${index}=${this.requestSourceData[dataIndex][1]}`);
            requestStr.push(`R${index}=${this.requestSourceData[dataIndex][2]}`);
        }

        this.requestCount = requestStr.length / 3;
        this.lastFetchedIndex = maxLen;

        return `ApplicationName=${appName}&${requestStr.join('&')}`;
        // load from parent window;
        // 브라우저 윈도우 이름으로 아래의 정보를 넘김
        // applicationName
        // oDragXY.fromX
        // oDragXY.toX
        // oDragXY.fromY
        // oDragXY.toY
        // agent
        // inclue?
        // Success/Failed 정보는 현재 window.opener.htoScatter['applicationName'] 에 저장해 둔 Scatter 객체가 가지고 있음.
        // window.htoScatter['']
        //
        // transactionList 데이터의 총합은 이전 window에서 가져옴.
        // indicator의 range는
        // 가져온 데이터 중 가장 예전의 collectorAcceptTime ~ endTime
        // 가져온 데이터의 갯수가 0 이거나 total 보다 커지면 끝
        // 요청 갯수와 응답 갯수가 다른 경우 total 값을 계속 update 해줘야 함.
        // 10개 transaction 데이터를 요청했는데 11개 혹은 12개가 오는 케이스가 있음.
        // --한번에 요청하는 갯수는 일단 최대 100개
        // const postBody = 'I0=FrontWAS2^1512710981192^11123&T0=1512718752125&R0=2&I1=FrontWAS3^1512711055472^10928&T1=1512718752105&R1=2&I2=FrontWAS2^1512710981192^11122&T2=1512718751113&R2=2&I3=FrontWAS3^1512711055472^10927&T3=1512718751092&R3=2&I4=FrontWAS1^1512711025001^3447&T4=1512718750828&R4=792&I5=FrontWAS4^1512711041898^3430&T5=1512718750448&R5=337&I6=FrontWAS4^1512711041898^3430&T6=1512718750440&R6=8&I7=FrontWAS2^1512710981192^11121&T7=1512718750103&R7=2&I8=FrontWAS3^1512711055472^10926&T8=1512718750081&R8=2&I9=FrontWAS2^1512710981192^11120&T9=1512718749819&R9=9&I10=FrontWAS2^1512710981192^11119&T10=1512718749621&R10=432&I11=FrontWAS2^1512710981192^11119&T11=1512718749617&R11=8&I12=FrontWAS3^1512711055472^10924&T12=1512718749564&R12=933&I13=FrontWAS4^1512711041898^3429&T13=1512718749558&R13=13&I14=FrontWAS2^1512710981192^11118&T14=1512718749091&R14=3&I15=FrontWAS3^1512711055472^10925&T15=1512718749070&R15=2&I16=FrontWAS2^1512710981192^11117&T16=1512718748080&R16=2&I17=FrontWAS3^1512711055472^10923&T17=1512718748059&R17=2&I18=FrontWAS2^1512710981192^11116&T18=1512718747069&R18=2&I19=FrontWAS3^1512711055472^10922&T19=1512718747047&R19=2';
        // return postBody;
    }
    private getInfoFromOpener(): any[] {
        if (this.windowRefService.nativeWindow.opener) {
            const {x1, x2, y1, y2, agentId, dotStatus} = JSON.parse(this.newUrlStateNotificationService.getQueryValue(UrlQuery.DRAG_INFO));
            const application = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getKeyStr();

            try {
                const scatterChartInstance = this.windowRefService.nativeWindow.opener.scatterChartInstance[application];

                if (scatterChartInstance) {
                    return scatterChartInstance.getDataByRange(x1, x2, y1, y2, agentId, dotStatus);
                }
            } catch (error) {
                return [];
            }
        }

        return this.checkUrlInfo();
    }
    private checkUrlInfo(): any[] {
        if (this.newUrlStateNotificationService.hasValue(UrlQuery.TRANSACTION_INFO)) {
            const {traceId, collectorAcceptTime, elapsed} = JSON.parse(this.newUrlStateNotificationService.getQueryValue(UrlQuery.TRANSACTION_INFO));

            return [[traceId, collectorAcceptTime, elapsed]];
        }
        return [];
    }

    // TODO: When v1 gets removed, no need to receive component parameter. Just use ServerError component.
    private onError({data, component}: {data: any, component: any}): void {
        this.dynamicPopupService.openPopup({
            data,
            component,
            onCloseCallback: () => {
                this.urlRouteManagerService.moveOnPage({
                    url: [
                        UrlPath.MAIN,
                        this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                        this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                        this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                    ],
                    queryParams: {
                        [UrlQuery.DRAG_INFO]: null,
                        [UrlQuery.TRANSACTION_INFO]: null
                    }
                });
            }
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
