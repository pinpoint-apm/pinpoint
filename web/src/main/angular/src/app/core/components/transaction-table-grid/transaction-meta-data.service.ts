import { Injectable, ComponentFactoryResolver, Injector } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Subject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { UrlPath, UrlPathId } from 'app/shared/models';
import {
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    WindowRefService,
    DynamicPopupService
} from 'app/shared/services';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Injectable()
export class TransactionMetaDataService {
    private requestURL = 'transactionmetadata.pinpoint';
    private retrieveErrorMessage: string;
    private lastFetchedIndex = 0;
    private maxLoadLength = 100;
    private requestSourceData: any[];
    private requestCount = 0;
    private countStatus = [0, 0];
    private outTransactionDataLoad: Subject<ITransactionMetaData[]> = new Subject();
    private outTransactionDataRange: Subject<number[]> = new Subject();
    private outTransactionDataCount: Subject<number[]> = new Subject();

    onTransactionDataLoad$: Observable<ITransactionMetaData[]>;
    onTransactionDataRange$: Observable<number[]>;
    onTransactionDataCount$: Observable<number[]>;

    constructor(
        private http: HttpClient,
        private windowRefService: WindowRefService,
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {
        this.onTransactionDataLoad$ = this.outTransactionDataLoad.asObservable();
        this.onTransactionDataRange$ = this.outTransactionDataRange.asObservable();
        this.onTransactionDataCount$ = this.outTransactionDataCount.asObservable();

        this.translateService.get('TRANSACTION_LIST.TRANSACTION_RETRIEVE_ERROR').subscribe((text: string) => {
            this.retrieveErrorMessage = text;
        });
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService && urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.PERIOD, UrlPathId.END_TIME);
            })
        ).subscribe(() => {
            this.requestSourceData = this.getInfoFromOpener();
            this.countStatus[1] = this.requestSourceData.length;
        });
    }
    loadData(): void {
        if (this.requestSourceData.length === 0) {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Notice',
                    contents: this.retrieveErrorMessage,
                },
                component: MessagePopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.moveOnPage({
                        url: [
                            UrlPath.MAIN,
                            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                        ]
                    });
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        } else {
            this.http.post<{ metadata: ITransactionMetaData[] }>(this.requestURL, this.makeRequestOptionsArgs(), {
                headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
            }).subscribe((responseData: { metadata: ITransactionMetaData[] }) => {
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
                    this.outTransactionDataLoad.next(responseData.metadata);
                }
                this.outTransactionDataCount.next(this.countStatus);
            }, (error: IServerErrorFormat) => {
                this.dynamicPopupService.openPopup({
                    data: {
                        title: 'Error',
                        contents: error
                    },
                    component: ServerErrorPopupContainerComponent,
                    onCloseCallback: () => {
                        this.urlRouteManagerService.reload();
                    }
                }, {
                    resolver: this.componentFactoryResolver,
                    injector: this.injector
                });
            });
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
    private makeRequestOptionsArgs(): string {
        const requestStr = [];
        const len = this.requestSourceData.length;
        const maxLen = Math.min(len, this.maxLoadLength + this.lastFetchedIndex);
        for ( let index = 0, dataIndex = this.lastFetchedIndex ; dataIndex < maxLen ; index++, dataIndex++ ) {
            // transactionId, x, y
            requestStr.push(`I${index}=${this.requestSourceData[dataIndex][0]}`);
            requestStr.push(`T${index}=${this.requestSourceData[dataIndex][1]}`);
            requestStr.push(`R${index}=${this.requestSourceData[dataIndex][2]}`);
        }
        this.requestCount = requestStr.length / 3;
        this.lastFetchedIndex = maxLen;
        return requestStr.join('&');
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
            const paramsInfo = this.windowRefService.nativeWindow.name.split('|');
            if (paramsInfo.length === 7) {
                const params = {
                    application: paramsInfo[0],
                    fromX: paramsInfo[1],
                    toX: paramsInfo[2],
                    fromY: paramsInfo[3],
                    toY: paramsInfo[4],
                    agent: paramsInfo[5],
                    types: paramsInfo[6].split(',')
                };
                try {
                    const scatterChartInstance = this.windowRefService.nativeWindow.opener.scatterChartInstance[params.application];
                    if (scatterChartInstance) {
                        return scatterChartInstance.getDataByRange(params.fromX, params.toX, params.fromY, params.toY, params.agent, params.types);
                    }
                } catch (error) {
                    return [];
                }
            }
        }
        return this.checkUrlInfo();
    }
    private checkUrlInfo(): any[] {
        if (this.newUrlStateNotificationService.hasValue(UrlPathId.TRANSACTION_INFO)) {
            const transactionInfo = this.newUrlStateNotificationService.getPathValue(UrlPathId.TRANSACTION_INFO).split('-');
            const length = transactionInfo.length;
            return [[
                transactionInfo.slice(0, length - 2).join('-'),
                transactionInfo[length - 2],
                transactionInfo[length - 1]
            ]];
        }
        return [];
    }
}
