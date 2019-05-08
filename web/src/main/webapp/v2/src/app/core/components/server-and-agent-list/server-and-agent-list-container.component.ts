import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { tap, pluck } from 'rxjs/operators';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    StoreHelperService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST,
} from 'app/shared/services';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';
import { InspectorPageService, ISourceForServerAndAgentList } from 'app/routes/inspector-page/inspector-page.service';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { filterServerList } from './server-and-agent-list-util';

@Component({
    selector: 'pp-server-and-agent-list-container',
    templateUrl: './server-and-agent-list-container.component.html',
    styleUrls: ['./server-and-agent-list-container.component.css'],
})
export class ServerAndAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    filterStr: string;
    agentId: string;
    serverList: { [key: string]: IServerAndAgentData[] };
    filteredServerList: { [key: string]: IServerAndAgentData[] };
    filteredServerKeyList: string[];
    funcImagePath: Function;

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
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        combineLatest(
            this.inspectorPageService.sourceForServerAndAgentList$.pipe(
                tap(({data, agentId}: ISourceForServerAndAgentList) => {
                    this.agentId = agentId;
                    this.serverList = data;
                }),
                pluck('data')
            ),
            this.storeHelperService.getServerAndAgentQuery<string>(this.unsubscribe)
        ).subscribe(([data, query]: [{[key: string]: IServerAndAgentData[]}, string]) => {
            this.filteredServerList = filterServerList(data, query, ({ agentId }: IServerAndAgentData) => agentId.toLowerCase().includes(query.toLowerCase()));
            this.filteredServerKeyList = Object.keys(this.filteredServerList).sort();
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
