import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { NewUrlStateNotificationService, StoreHelperService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { Filter } from 'app/core/models';

@Component({
    selector: 'pp-filter-information-container',
    templateUrl: './filter-information-container.component.html',
    styleUrls: ['./filter-information-container.component.css']
})
export class FilterInformationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private serverMapData: any;
    private selectedTarget: ISelectedTarget;
    filterInfo: Filter[];
    filterIndexOfCurrentLink: number;
    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.FILTER);
            })
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.filterInfo = Filter.instanceFromString(urlService.getPathValue(UrlPathId.FILTER));
        });
        this.connectStore();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: any) => {
            this.serverMapData = serverMapData;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
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
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
