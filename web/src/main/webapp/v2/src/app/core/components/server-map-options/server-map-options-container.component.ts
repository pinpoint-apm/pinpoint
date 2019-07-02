import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, tap, map } from 'rxjs/operators';

import {
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { UrlQuery, UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-server-map-options-container',
    templateUrl: './server-map-options-container.component.html',
    styleUrls: ['./server-map-options-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapOptionsContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();

    funcImagePath: Function;
    hiddenComponent: boolean;
    inboundList: number[];
    outboundList: number[];
    selectedInbound: number;
    selectedOutbound: number;
    selectedBidirectional: boolean;
    selectedWasOnly: boolean;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService
    ) {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
    }

    ngOnInit() {
        this.inboundList = this.webAppSettingDataService.getInboundList();
        this.outboundList = this.webAppSettingDataService.getOutboundList();

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                if (urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.PERIOD, UrlPathId.END_TIME)) {
                    this.hiddenComponent = false;
                } else {
                    this.hiddenComponent = true;
                }
            }),
            map((urlService: NewUrlStateNotificationService) => {
                return {
                    inbound: urlService.hasValue(UrlQuery.INBOUND) ? urlService.getQueryValue(UrlQuery.INBOUND) : this.webAppSettingDataService.getUserDefaultInbound(),
                    outbound: urlService.hasValue(UrlQuery.OUTBOUND) ? urlService.getQueryValue(UrlQuery.OUTBOUND) : this.webAppSettingDataService.getUserDefaultOutbound(),
                    bidirectional: urlService.hasValue(UrlQuery.BIDIRECTIONAL) ? urlService.getQueryValue(UrlQuery.BIDIRECTIONAL) : false,
                    wasOnly: urlService.hasValue(UrlQuery.WAS_ONLY) ? urlService.getQueryValue(UrlQuery.WAS_ONLY) : false
                };
            })
        ).subscribe(({inbound, outbound, bidirectional, wasOnly}: {inbound: number, outbound: number, bidirectional: boolean, wasOnly: boolean}) => {
            this.selectedInbound = inbound;
            this.selectedOutbound = outbound;
            this.selectedBidirectional = bidirectional;
            this.selectedWasOnly = wasOnly;
            this.changeDetectorRef.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onChangeBound(options: { inbound: number, outbound: number, wasOnly: boolean, bidirectional: boolean }): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_SERVER_MAP_OPTION, `inbound: ${options.inbound}, outbound: ${options.outbound}, wasOnly: ${options.wasOnly}, bidirectional: ${options.bidirectional}`);
        this.urlRouteManagerService.moveOnPage({
            url: [
                this.newUrlStateNotificationService.getStartPath(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
            ],
            queryParam: {
                [UrlQuery.INBOUND]: options.inbound,
                [UrlQuery.OUTBOUND]: options.outbound,
                [UrlQuery.WAS_ONLY]: options.wasOnly,
                [UrlQuery.BIDIRECTIONAL]: options.bidirectional
            }
        });
    }
}
