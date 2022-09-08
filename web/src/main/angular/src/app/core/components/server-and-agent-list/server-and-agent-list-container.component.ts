import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, iif, of, Observable, EMPTY, merge } from 'rxjs';
import { tap, concatMap, filter, switchMap, delay, takeUntil, catchError, map, pluck } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO,
    TranslateReplaceService,
} from 'app/shared/services';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';
import { isEmpty, isThatType } from 'app/core/utils/util';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-server-and-agent-list-container',
    templateUrl: './server-and-agent-list-container.component.html',
    styleUrls: ['./server-and-agent-list-container.component.css'],
})
export class ServerAndAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';

    agentId: string;
    serverList: {[key: string]: IServerAndAgentData[]};
    filteredServerList: {[key: string]: IServerAndAgentData[]} = {};
    filteredServerKeyList: string[] = [];
    funcImagePath: Function;
    isEmpty: boolean;
    emptyText$: Observable<string>;
    errorMessage: string;
    inputPlaceholder$: Observable<string>;
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private serverAndAgentListDataService: ServerAndAgentListDataService,
        private messageQueueService: MessageQueueService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();

        merge(
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                takeUntil(this.unsubscribe),
                tap((urlService: NewUrlStateNotificationService) => {
                    this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
                }),
                // TODO: Check valid filter for url
                // filter((urlService: NewUrlStateNotificationService) => {
                //     return urlService.isValueChanged(UrlPathId.APPLICATION) || urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME);
                // }),
                map((urlService: NewUrlStateNotificationService) => {
                    if (urlService.isRealTimeMode()) {
                        const to = urlService.getUrlServerTimeData();
                        const from = to - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();

                        return [from, to];
                    } else {
                        return [urlService.getStartTimeToNumber(), urlService.getEndTimeToNumber()];
                    }

                })
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.FETCH_AGENT_LIST).pipe(
                switchMap((data: {range: number[], emitAfter: number}) => {
                    return iif(() => data.emitAfter === 0,
                        of(data),
                        of(data).pipe(delay(data.emitAfter))
                    );
                }),
                pluck('range'),
            )
        ).pipe(
            filter(() => this.newUrlStateNotificationService.hasValue(UrlPathId.APPLICATION)), // prevent getting event after the component has been destroyed
            concatMap((range: number[]) => {
                const appName = (this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION) as IApplication).getApplicationName();
                const requestStartAt = Date.now();

                return this.serverAndAgentListDataService.getData(appName, range).pipe(
                    filter((res: {[key: string]: IServerAndAgentData[]} | IServerErrorShortFormat) => {
                        // TODO: 민우님께 에러구분 여쭤보기. 401이면 AuthService 활용한다? 근데이럼 IS_ACCESS_DENYED 출처 불분명같은 문제가 있지않을까..
                        if (isThatType<IServerErrorShortFormat>(res, 'errorCode', 'errorMessage')) {
                            this.errorMessage = res.errorMessage;
                            this.messageQueueService.sendMessage({to: MESSAGE_TO.IS_ACCESS_DENYED, param: true});
                            return false;
                        } else {
                            this.errorMessage = '';
                            this.messageQueueService.sendMessage({to: MESSAGE_TO.IS_ACCESS_DENYED, param: false});
                            return true;
                        }
                    }),
                    filter((res: {[key: string]: IServerAndAgentData[]}) => {
                        if (this.agentId) {
                            const filteredList = this.filterServerList(res, this.agentId, ({agentId}: IServerAndAgentData) => this.agentId.toLowerCase() === agentId.toLowerCase());
                            const isAgentIdValid = Object.keys(filteredList).length !== 0;

                            if (isAgentIdValid) {
                                return true;
                            } else {
                                const url = this.newUrlStateNotificationService.isRealTimeMode()
                                    ? [this.newUrlStateNotificationService.getStartPath(), UrlPathId.REAL_TIME, this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr()]
                                    : [
                                        this.newUrlStateNotificationService.getStartPath(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                                    ];

                                this.urlRouteManagerService.moveOnPage({url});

                                return false;
                            }
                        } else {
                            return true;
                        }
                    }),
                    tap(() => {
                        const responseArriveAt = Date.now();
                        const deltaT = responseArriveAt - requestStartAt;
                        const now = range[1] + deltaT;

                        this.messageQueueService.sendMessage({
                            to: MESSAGE_TO.AGENT_LIST_VALID,
                            param: {
                                range,
                                now,
                                agentId: this.agentId
                            }
                        });
                    }),
                    catchError((error: IServerError) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });

                        return EMPTY;
                    })
                );
            }),
        ).subscribe((data: {[key: string]: IServerAndAgentData[]}) => {
            this.serverList = data;
            this.filteredServerList = this.filterServerList(data, this.query);
            this.filteredServerKeyList = Object.keys(this.filteredServerList).sort();
            this.isEmpty = isEmpty(this.filteredServerList);
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18nText(): void {
        this.emptyText$ = this.translateService.get('COMMON.EMPTY_ON_SEARCH');
        this.inputPlaceholder$ = this.translateService.get('COMMON.MIN_LENGTH').pipe(
            map((text: string) => this.translateReplaceService.replace(text, this.SEARCH_MIN_LENGTH))
        );
    }

    onSelectAgent(agentId: string) {
        const url = this.newUrlStateNotificationService.isRealTimeMode() ?
            [
                this.newUrlStateNotificationService.getStartPath(),
                UrlPath.REAL_TIME,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                agentId
            ] :
            [
                this.newUrlStateNotificationService.getStartPath(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                agentId
            ];

        this.urlRouteManagerService.moveOnPage({url});
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_AGENT_ON_THE_LIST);
    }

    private filterServerList(serverList: {[key: string]: IServerAndAgentData[]}, query: string, predi?: (data: IServerAndAgentData) => boolean): {[key: string]: IServerAndAgentData[]} {
        const filterCallback = predi ? predi : ({agentId, agentName}: IServerAndAgentData) => {
            return agentId.toLowerCase().includes(query.toLowerCase()) || (agentName && agentName.toLowerCase().includes(query.toLowerCase()));
        };

        return query === '' ? serverList
            : Object.entries(serverList).reduce((acc: {[key: string]: IServerAndAgentData[]}, [key, serverAndAgentDataList]: [string, IServerAndAgentData[]]) => {
                const matchedList = serverAndAgentDataList.filter(filterCallback);
                
                return isEmpty(matchedList) ? acc : {...acc, [key]: matchedList};
            }, {} as {[key: string]: IServerAndAgentData[]});
    }

    private set query(query: string) {
        this._query = query;
        this.filteredServerList = this.filterServerList(this.serverList, query);
        this.filteredServerKeyList = Object.keys(this.filteredServerList).sort();
        this.isEmpty = isEmpty(this.filteredServerList);
    }

    private get query(): string {
        return this._query;
    }

    onSearch(query: string): void {
        if (this.query === query) {
            return;
        }

        this.query = query;
    }

    onCancel(): void {
        this.query = '';
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.AGENT_LIST);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.AGENT_LIST,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
