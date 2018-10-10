import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { switchMap, takeUntil, filter } from 'rxjs/operators';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    StoreHelperService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-server-and-agent-list-container',
    templateUrl: './server-and-agent-list-container.component.html',
    styleUrls: ['./server-and-agent-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
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
        private changeDetectorRef: ChangeDetectorRef,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
        private serverAndAgentListDataService: ServerAndAgentListDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.END_TIME);
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                this.agentId = urlService.hasValue(UrlPathId.AGENT_ID) ? urlService.getPathValue(UrlPathId.AGENT_ID) : '';
                return this.serverAndAgentListDataService.getData();
            })
        ).subscribe((res: {[key: string]: IServerAndAgentData[]}) => {
            this.serverKeyList = this.filteredServerKeyList = Object.keys(res).sort();
            this.serverList = this.filteredServerList = res;
            if (this.agentId) {
                this.dispatchAgentData();
            }
            this.changeDetectorRef.detectChanges();
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            });
        });
        this.storeHelperService.getServerAndAgentQuery<string>(this.unsubscribe).subscribe((query: string) => {
            this.filteringServerList(query);
            this.changeDetectorRef.detectChanges();
        });
    }
    private dispatchAgentData(): void {
        this.serverKeyList.forEach((key: string) => {
            this.serverList[key].forEach((agent: IServerAndAgentData) => {
                if ( this.agentId === agent.agentId ) {
                    this.storeHelperService.dispatch(new Actions.UpdateAgentInfo(agent));
                }
            });
        });
    }
    private filteringServerList(query: string): void {
        if ( query === '' ) {
            this.filteredServerKeyList = this.serverKeyList;
            this.filteredServerList = this.serverList;
        } else {
            const filteredKeyList: string[] = [];
            const filteredServerList: { [key: string]: IServerAndAgentData[] } = {};
            this.serverKeyList.forEach((key: string) => {
                let hasKey = false;
                this.serverList[key].forEach((agent: IServerAndAgentData) => {
                    if ( agent.agentId.toLowerCase().indexOf(query.toLowerCase()) !== -1 ) {
                        if ( hasKey === false ) {
                            filteredKeyList.push(key);
                            filteredServerList[key] = [];
                            hasKey = true;
                        }
                        filteredServerList[key].push(agent);
                    }
                });
            });
            this.filteredServerKeyList = filteredKeyList.sort();
            this.filteredServerList = filteredServerList;
        }
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onSelectAgent(agentName: string) {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.GO_TO_AGENT_INSPECTOR);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                agentName
            ]
        });
    }
}
