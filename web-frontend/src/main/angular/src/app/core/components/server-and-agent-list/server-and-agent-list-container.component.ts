import {Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector} from '@angular/core';
import {Subject, iif, of, Observable, EMPTY, merge} from 'rxjs';
import {tap, concatMap, filter, switchMap, delay, takeUntil, catchError, map, pluck} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';

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
import {
    ServerErrorPopupContainerComponent
} from 'app/core/components/server-error-popup/server-error-popup-container.component';
import {UrlPath, UrlPathId} from 'app/shared/models';
import {ServerAndAgentListDataService} from './server-and-agent-list-data.service';
import {isEmpty, isThatType} from 'app/core/utils/util';
import {
    HELP_VIEWER_LIST,
    HelpViewerPopupContainerComponent
} from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

export const enum SortOption {
    ID = 'id',
    NAME = 'name',
    RECENT = 'recent'
}

@Component({
    selector: 'pp-server-and-agent-list-container',
    templateUrl: './server-and-agent-list-container.component.html',
    styleUrls: ['./server-and-agent-list-container.component.css'],
})
export class ServerAndAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';
    private previousParams: { app: string, range: number[] };
    private cachedData = {} as { [key in SortOption]: IServerAndAgentDataV2[] };

    agentId: string;
    serverList: IServerAndAgentDataV2[];
    filteredServerList: IServerAndAgentDataV2[] = [];
    filteredServerKeyList: string[] = [];
    funcImagePath: Function;
    isEmpty: boolean;
    emptyText$: Observable<string>;
    errorMessage: string;
    inputPlaceholder$: Observable<string>;
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    sortOptionList = [
        {display: 'ID', key: SortOption.ID},
        {display: 'Name', key: SortOption.NAME},
        {display: 'Recent', key: SortOption.RECENT}
    ];
    selectedSortOptionKey: SortOption;

    useDisable: boolean;
    showLoading: boolean;

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
    ) {
    }

    ngOnInit() {
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        this.selectedSortOptionKey = this.webAppSettingDataService.getAgentListSortOption() as SortOption || SortOption.ID;

        merge(
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                takeUntil(this.unsubscribe),
                tap((urlService: NewUrlStateNotificationService) => {
                    this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
                    const isAppChanged = urlService.isValueChanged(UrlPathId.APPLICATION);
                    const isPeriodChanged = urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME);
                    const isRealTimeMode = urlService.isRealTimeMode();

                    // if (isAppChanged || isPeriodChanged || isRealTimeMode) {

                    if (isAppChanged) {
                        this.filteredServerList = [];
                        this.previousParams = null;
                        this.showLoading = true;
                    } else if (isPeriodChanged && !isEmpty(this.filteredServerList)) {
                        this.previousParams = null;
                        this.showLoading = true;
                        this.useDisable = true;
                    }

                }),
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
                switchMap((data: { range: number[], emitAfter: number }) => {
                    return iif(() => data.emitAfter === 0,
                        of(data),
                        of(data).pipe(delay(data.emitAfter))
                    );
                }),
                pluck('range'),
                filter(() => this.newUrlStateNotificationService.isRealTimeMode()),
            )
        ).pipe(
            filter(() => this.newUrlStateNotificationService.hasValue(UrlPathId.APPLICATION)), // prevent from getting event after the component has been destroyed
            concatMap((range: number[]) => {
                const urlService = this.newUrlStateNotificationService;
                const isAppChanged = urlService.isValueChanged(UrlPathId.APPLICATION);
                const isPeriodChanged = urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME);
                const isRealTimeMode = urlService.isRealTimeMode();

                const data = this.cachedData[this.selectedSortOptionKey];

                const app = (urlService.getPathValue(UrlPathId.APPLICATION) as IApplication).getApplicationName();
                const requestStartAt = Date.now();

                return iif(() => isEmpty(this.filteredServerList) || (isAppChanged || isPeriodChanged || isRealTimeMode),
                    this.serverAndAgentListDataService.getData(app, range, this.selectedSortOptionKey).pipe(
                        tap(() => {
                            this.previousParams = {app, range};
                            this.cachedData = {} as { [key in SortOption]: IServerAndAgentDataV2[] };
                        }),
                        filter((res: IServerAndAgentDataV2[] | IServerErrorShortFormat) => {
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
                    ),
                    of(data)
                ).pipe(
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
                )
            })
        ).subscribe((data: IServerAndAgentDataV2[]) => {
            this.serverList = this.cachedData[this.selectedSortOptionKey] = data;
            this.filteredServerList = this.filterServerList(this.serverList, this.query);
            this.filteredServerKeyList = this.filteredServerList.map(({groupName}: IServerAndAgentDataV2) => groupName).sort();
            this.isEmpty = isEmpty(this.filteredServerList);

            this.useDisable = false;
            this.showLoading = false;
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
    }

    private filterServerList(serverList: IServerAndAgentDataV2[], query: string, predi?: (data: IAgentDataV2) => boolean): IServerAndAgentDataV2[] {
        const filterCallback = predi ? predi : ({agentId, agentName}: IAgentDataV2) => {
            return agentId.toLowerCase().includes(query.toLowerCase()) || (agentName && agentName.toLowerCase().includes(query.toLowerCase()));
        };

        return query === '' ? serverList
            : serverList.reduce((acc: IServerAndAgentDataV2[], {groupName, instancesList}: IServerAndAgentDataV2) => {
                const matchedList = instancesList.filter(filterCallback);

                return isEmpty(matchedList) ? acc : [...acc, {groupName, instancesList: matchedList}]
            }, []);
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

    isActiveSortOption(optionKey: SortOption): boolean {
        return optionKey === this.selectedSortOptionKey;
    }

    onSelectSortOption(optionKey: SortOption): void {
        if (optionKey === this.selectedSortOptionKey || this.showLoading) {
            return;
        }

        const {app, range} = this.previousParams;

        of(optionKey).pipe(
            switchMap((optionKey: SortOption) => {
                if (Boolean(this.cachedData[optionKey])) {
                    return of(this.cachedData[optionKey]);
                } else {
                    this.useDisable = true;
                    this.showLoading = true;

                    return this.serverAndAgentListDataService.getData(app, range, optionKey).pipe(
                        tap((data: IServerAndAgentDataV2[]) => {
                            this.useDisable = false;
                            this.showLoading = false;
                            this.serverList = this.cachedData[optionKey] = data;
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
                }
            })
        ).subscribe((data: IServerAndAgentDataV2[]) => {
            this.filteredServerList = this.filterServerList(data, this.query);
            this.filteredServerKeyList = this.filteredServerList.map(({groupName}: IServerAndAgentDataV2) => groupName).sort();
            this.isEmpty = isEmpty(this.filteredServerList);
            this.selectedSortOptionKey = optionKey;
            this.webAppSettingDataService.setAgentListSortOption(optionKey);
        });
    }
}
