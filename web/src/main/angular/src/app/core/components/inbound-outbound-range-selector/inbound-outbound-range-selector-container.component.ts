import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, tap, map } from 'rxjs/operators';

import { UrlQuery, UrlPathId } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService, UrlRouteManagerService } from 'app/shared/services';

@Component({
    selector: 'pp-inbound-outbound-range-selector-container',
    templateUrl: './inbound-outbound-range-selector-container.component.html',
    styleUrls: ['./inbound-outbound-range-selector-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InboundOutboundRangeSelectorContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();

    hiddenComponent: boolean;
    inboundList: number[];
    outboundList: number[];
    selectedInbound: number;
    selectedOutbound: number;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}

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
                    outbound: urlService.hasValue(UrlQuery.OUTBOUND) ? urlService.getQueryValue(UrlQuery.OUTBOUND) : this.webAppSettingDataService.getUserDefaultOutbound()
                };
            })
        ).subscribe(({inbound, outbound}: {inbound: number, outbound: number}) => {
            this.selectedInbound = inbound;
            this.selectedOutbound = outbound;
            this.changeDetectorRef.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onChangeBound(bound: string[]): void {
        this.urlRouteManagerService.moveOnPage({
            url: [
                this.newUrlStateNotificationService.getStartPath(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
            ],
            queryParam: {
                [UrlQuery.INBOUND]: bound[0],
                [UrlQuery.OUTBOUND]: bound[1]
            }
        });
    }
}
