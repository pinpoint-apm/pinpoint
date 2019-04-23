import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { switchMap, takeUntil, filter, tap, map } from 'rxjs/operators';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    StoreHelperService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST,
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-server-and-agent-list-container',
    templateUrl: './server-and-agent-list-container.component.html',
    styleUrls: ['./server-and-agent-list-container.component.css'],
})
export class ServerAndAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    filterStr: string;
    agentId: string;
    serverList: { [key: string]: IServerAndAgentData[] } = {};
    serverKeyList: string[];
    filteredServerList: { [key: string]: IServerAndAgentData[] };
    filteredServerKeyList: string[];
    funcImagePath: Function;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
        private serverAndAgentListDataService: ServerAndAgentListDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}
    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();

        combineLatest(
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                takeUntil(this.unsubscribe),
                tap((urlService: NewUrlStateNotificationService) => {
                    this.agentId = urlService.hasValue(UrlPathId.AGENT_ID) ? urlService.getPathValue(UrlPathId.AGENT_ID) : '';
                }),
                filter((urlService: NewUrlStateNotificationService) => {
                    return urlService.isValueChanged(UrlPathId.APPLICATION);
                }),
                map((urlService: NewUrlStateNotificationService) => {
                    return (urlService.getPathValue(UrlPathId.APPLICATION) as IApplication).getApplicationName();
                })
            ),
            this.storeHelperService.getRange(this.unsubscribe).pipe(
                filter((range: number[]) => !!range)
            )
        ).pipe(
            switchMap(([applicationName, range]: [string, number[]]) => {
                return this.serverAndAgentListDataService.getData(applicationName, range);
            }),
            tap((res: {[key: string]: IServerAndAgentData[]}) => {
                if (this.agentId) {
                    const filteredList = this.filterServerList(res, this.agentId, ({ agentId }: IServerAndAgentData) => agentId.toLowerCase() === this.agentId.toLowerCase());

                    if (Object.keys(filteredList).length === 0) {
                        const url = this.newUrlStateNotificationService.isRealTimeMode()
                            ? [ UrlPath.INSPECTOR, UrlPathId.REAL_TIME, this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr() ]
                            : [
                                UrlPath.INSPECTOR,
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                            ];

                        this.urlRouteManagerService.moveOnPage({ url });
                    }
                }
            })
        ).subscribe((res: {[key: string]: IServerAndAgentData[]}) => {
            this.serverKeyList = this.filteredServerKeyList = Object.keys(res).sort();
            this.serverList = this.filteredServerList = res;
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

        this.storeHelperService.getServerAndAgentQuery<string>(this.unsubscribe).pipe(
            filter((query: string) => !!query)
        ).subscribe((query: string) => {
            this.filteredServerList = this.filterServerList(this.serverList, query, ({ agentId }: IServerAndAgentData) => agentId.toLowerCase().includes(query.toLowerCase()));
            this.filteredServerKeyList = Object.keys(this.filteredServerList).sort();
        });
    }
    private filterServerList(serverList: { [key: string]: IServerAndAgentData[] }, query: string, predi: any): { [key: string]: IServerAndAgentData[] } {
        return query === ''
            ? serverList
            : Object.keys(serverList).reduce((acc: { [key: string]: IServerAndAgentData[] }, key: string) => {
                const matchedList = serverList[key].filter(predi);

                return matchedList.length !== 0 ? { ...acc, [key]: matchedList } : acc;
            }, {} as { [key: string]: IServerAndAgentData[] });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
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
}
