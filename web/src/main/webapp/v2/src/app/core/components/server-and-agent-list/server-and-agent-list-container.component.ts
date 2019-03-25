import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject } from 'rxjs';
import { switchMap, takeUntil, filter, tap, skip } from 'rxjs/operators';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    StoreHelperService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST
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
    filteredServerKeyList: string[] = [];
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
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                this.agentId = urlService.hasValue(UrlPathId.AGENT_ID) ? urlService.getPathValue(UrlPathId.AGENT_ID) : '';
            }),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.isValueChanged(UrlPathId.APPLICATION) || urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME);
            }),
            switchMap(() => {
                return this.serverAndAgentListDataService.getData();
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
            skip(1)
        ).subscribe((query: string) => {
            this.filteringServerList(query);
        });
    }
    private filteringServerList(query: string): void {
        if (query === '') {
            this.filteredServerKeyList = this.serverKeyList;
            this.filteredServerList = this.serverList;
        } else {
            this.filteredServerList = Object.keys(this.serverList).reduce((acc: { [key: string]: IServerAndAgentData[] }, key: string) => {
                const matchedList = this.serverList[key].filter(({ agentId }: IServerAndAgentData) => agentId.toLowerCase().includes(query.toLowerCase()));

                return matchedList.length !== 0 ? { ...acc, [key]: matchedList } : acc;
            }, {} as { [key: string]: IServerAndAgentData[] });
            this.filteredServerKeyList = Object.keys(this.filteredServerList).sort();
        }
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onSelectAgent(agentId: string) {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.GO_TO_AGENT_INSPECTOR);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                agentId
            ]
        });
    }
}
