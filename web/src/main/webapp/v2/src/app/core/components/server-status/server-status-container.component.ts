import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { StoreHelperService, NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

@Component({
    selector: 'pp-server-status-container',
    templateUrl: './server-status-container.component.html',
    styleUrls: ['./server-status-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerStatusContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private enableRealTime: boolean;
    node: INodeInfo;
    isInfoPerServerShow = false;
    isLoading = false;
    serverMapData: ServerMapData;
    selectedTarget: ISelectedTarget;
    constructor(
        private changeDetector: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.enableRealTime = urlService.isRealTimeMode();
        });
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return (target) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.selectedTarget = target;
            this.node = (target.isNode === true ? this.serverMapData.getNodeData(target.node[0]) : null);
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getInfoPerServerState(this.unsubscribe).subscribe((visibleState: boolean) => {
            this.isLoading = false;
            this.isInfoPerServerShow = visibleState;
            this.changeDetector.detectChanges();
        });
    }
    onClickViewServer(): void {
        if (this.isLoading === true || this.enableRealTime === true) {
            return;
        }
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_SERVER_LIST);
        this.isLoading = !this.isLoading;
        this.storeHelperService.dispatch(new Actions.ChangeServerMapDisableState(!this.isInfoPerServerShow));
        this.storeHelperService.dispatch(new Actions.ChangeInfoPerServerVisibleState(!this.isInfoPerServerShow));
    }
    hasServerList(): boolean {
        if (this.selectedTarget) {
            if (this.selectedTarget.isNode && this.selectedTarget.isMerged === false) {
                return this.selectedTarget.hasServerList;
            }
        }
        return false;
    }
    enableViewServer(): boolean {
        return !this.enableRealTime;
    }
    onClickOpenInspector(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_INSPECTOR);
        this.urlRouteManagerService.openInspectorPage(this.enableRealTime);
    }
    getAngle(): string {
        return this.isInfoPerServerShow ? 'right' : 'left';
    }
    isWAS(): boolean {
        return this.selectedTarget.isWAS;
    }
}
