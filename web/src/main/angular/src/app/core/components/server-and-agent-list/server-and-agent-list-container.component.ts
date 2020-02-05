import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, combineLatest, iif, of, Observable } from 'rxjs';
import { tap, concatMap, filter, switchMap, delay, takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    StoreHelperService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO,
} from 'app/shared/services';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { InspectorPageService, ISourceForServerAndAgentList } from 'app/routes/inspector-page/inspector-page.service';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-server-and-agent-list-container',
    templateUrl: './server-and-agent-list-container.component.html',
    styleUrls: ['./server-and-agent-list-container.component.css'],
})
export class ServerAndAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    filterStr: string;
    agentId: string;
    serverList: { [key: string]: IServerAndAgentData[] };
    filteredServerList: { [key: string]: IServerAndAgentData[] };
    filteredServerKeyList: string[];
    funcImagePath: Function;
    isEmpty: boolean;
    emptyText$: Observable<string>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private inspectorPageService: InspectorPageService,
        private serverAndAgentListDataService: ServerAndAgentListDataService,
        private messageQueueService: MessageQueueService,
        private translateService: TranslateService,
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        combineLatest(
            this.inspectorPageService.sourceForServerAndAgentList$.pipe(
                takeUntil(this.unsubscribe),
                filter((data: ISourceForServerAndAgentList) => !!data),
                switchMap((data: ISourceForServerAndAgentList) => {
                    return iif(() => data.emitAfter === 0,
                        of(data),
                        of(data).pipe(delay(data.emitAfter))
                    );
                }),
                tap(({agentId}: ISourceForServerAndAgentList) => {
                    this.agentId = agentId;
                }),
                concatMap(({range}: ISourceForServerAndAgentList) => {
                    const appName = (this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION) as IApplication).getApplicationName();
                    const requestStartAt = Date.now();

                    return this.serverAndAgentListDataService.getData(appName, range).pipe(
                        filter((res: {[key: string]: IServerAndAgentData[]}) => {
                            if (this.agentId) {
                                const filteredList = this.filterServerList(res, this.agentId, ({ agentId }: IServerAndAgentData) => this.agentId.toLowerCase() === agentId.toLowerCase());
                                const isAgentIdValid = Object.keys(filteredList).length !== 0;

                                if (isAgentIdValid) {
                                    return true;
                                } else {
                                    const url = this.newUrlStateNotificationService.isRealTimeMode()
                                        ? [ UrlPath.INSPECTOR, UrlPathId.REAL_TIME, this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr() ]
                                        : [
                                            UrlPath.INSPECTOR,
                                            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                                            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                                            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                                        ];

                                    this.urlRouteManagerService.moveOnPage({ url });

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
                                to: MESSAGE_TO.INSPECTOR_PAGE_VALID,
                                param: {range, now}
                            });
                        })
                    );
                }),
            ),
            this.storeHelperService.getServerAndAgentQuery<string>(this.unsubscribe)
        ).subscribe(([data, query]: [{[key: string]: IServerAndAgentData[]}, string]) => {
            this.filteredServerList = this.filterServerList(data, query, ({ agentId }: IServerAndAgentData) => agentId.toLowerCase().includes(query.toLowerCase()));
            this.filteredServerKeyList = Object.keys(this.filteredServerList).sort();
            this.isEmpty = isEmpty(this.filteredServerList);
        }, (error: IServerErrorFormat) => {
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
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18nText(): void {
        this.emptyText$ = this.translateService.get('COMMON.EMPTY_ON_SEARCH');
    }

    onSelectAgent(agentId: string) {
        const url = this.newUrlStateNotificationService.isRealTimeMode() ?
            [
                UrlPath.INSPECTOR,
                UrlPath.REAL_TIME,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                agentId
            ] :
            [
                UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                agentId
            ];

        this.urlRouteManagerService.moveOnPage({ url });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.GO_TO_AGENT_INSPECTOR);
    }

    private filterServerList(serverList: { [key: string]: IServerAndAgentData[] }, query: string, predi: any): { [key: string]: IServerAndAgentData[] } {
        return query === ''
            ? serverList
            : Object.keys(serverList).reduce((acc: { [key: string]: IServerAndAgentData[] }, key: string) => {
                const matchedList = serverList[key].filter(predi);

                return matchedList.length !== 0 ? { ...acc, [key]: matchedList } : acc;
            }, {} as { [key: string]: IServerAndAgentData[] });
    }
}
