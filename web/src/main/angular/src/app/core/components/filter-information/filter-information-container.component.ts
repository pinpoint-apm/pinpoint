import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { NewUrlStateNotificationService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { UrlQuery } from 'app/shared/models';
import { Filter } from 'app/core/models';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

@Component({
    selector: 'pp-filter-information-container',
    templateUrl: './filter-information-container.component.html',
    styleUrls: ['./filter-information-container.component.css']
})
export class FilterInformationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private serverMapData: any;
    private selectedTarget: ISelectedTarget;

    filterInfo: Filter[];
    filterIndexOfCurrentLink: number;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private messageQueueService: MessageQueueService,
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlQuery.FILTER))
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.filterInfo = Filter.instanceFromString(urlService.getQueryValue(UrlQuery.FILTER));
        });
        this.listenToEmitter();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private listenToEmitter(): void {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).subscribe((data: ServerMapData) => {
            this.serverMapData = data;
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).subscribe((target: ISelectedTarget) => {
            this.filterIndexOfCurrentLink = -1;
            this.selectedTarget = target;
        });
    }

    showFilterInfo(): boolean {
        if (this.selectedTarget) {
            if (this.selectedTarget.isLink === true && this.selectedTarget.isMerged === false) {
                const link = this.serverMapData.getLinkData(this.selectedTarget.link[0]);
                if (this.isFilterLink(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    isFilterLink(link: any): boolean {
        for (let i = 0 ; i < this.filterInfo.length ; i++) {
            const f = this.filterInfo[i];
            if ((f.fromApplication + '^' + f.fromServiceType) === link.from && (f.toApplication + '^' + f.toServiceType) === link.to) {
                this.filterIndexOfCurrentLink = i;
                return true;
            }
        }
        return false;
    }

    getAgentFrom(): string {
        return this.filterInfo[this.filterIndexOfCurrentLink].fromAgentName || 'All';
    }

    getAgentTo(): string {
        return this.filterInfo[this.filterIndexOfCurrentLink].toAgentName || 'All';
    }

    getUrlPattern(): string {
        return this.filterInfo[this.filterIndexOfCurrentLink].urlPattern || 'none';
    }

    getResponseTimeFrom(): number {
        return this.filterInfo[this.filterIndexOfCurrentLink].responseFrom || 0;
    }

    getResponseTimeTo(): number {
        return this.filterInfo[this.filterIndexOfCurrentLink].responseTo || 30000;
    }

    getTransactionResult(): string {
        return this.filterInfo[this.filterIndexOfCurrentLink].getTransactionResultStr();
    }
}
