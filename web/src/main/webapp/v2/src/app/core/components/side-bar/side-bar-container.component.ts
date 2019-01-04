import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { StoreHelperService, NewUrlStateNotificationService } from 'app/shared/services';

@Component({
    selector: 'pp-side-bar-container',
    templateUrl: './side-bar-container.component.html',
    styleUrls: ['./side-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    target: any;
    sideBarWidth = 0;
    useDisable = true;
    showLoading = true;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.APPLICATION)) {
                this.showLoading = true;
                this.useDisable = true;
            } else {
                this.sideBarWidth = 0;
                this.showLoading = false;
                this.useDisable = false;
            }
            this.changeDetectorRef.detectChanges();
        });
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapData(this.unsubscribe).pipe(
            filter((target: any) => {
                return target.nodeList ? true : false;
            })
        ).subscribe((target: any) => {
            if (target.nodeList.length === 0) {
                this.sideBarWidth = 0;
            }
            this.changeDetectorRef.detectChanges();
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.target = target;
            if (target.isNode === true || target.isLink === true) {
                this.sideBarWidth = 461;
            } else {
                this.sideBarWidth = 0;
            }
            this.showLoading = false;
            this.useDisable = false;
            this.changeDetectorRef.detectChanges();
        });
    }
    hasTopElement(): boolean {
        return this.target && (this.target.isNode || this.target.isMerged);
    }
}
