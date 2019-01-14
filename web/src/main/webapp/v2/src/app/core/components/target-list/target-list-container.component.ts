import { Component, OnInit, OnDestroy, AfterViewInit, AfterViewChecked, ElementRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { UrlPathId } from 'app/shared/models';
import { Filter } from 'app/core/models/filter';
import {
    UrlRouteManagerService,
    StoreHelperService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { FilterTransactionWizardPopupContainerComponent } from 'app/core/components/filter-transaction-wizard-popup/filter-transaction-wizard-popup-container.component';

@Component({
    selector: 'pp-target-list-container',
    templateUrl: './target-list-container.component.html',
    styleUrls: ['./target-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TargetListContainerComponent implements OnInit, OnDestroy, AfterViewInit, AfterViewChecked {
    minLength = 2;
    isLink = false;
    filterQuery = '';
    selectedTarget: ISelectedTarget;
    serverMapData: ServerMapData;
    notFilteredTargetList: any[];
    targetList: any[];
    unsubscribe: Subject<null> = new Subject();
    userInputChange = new Subject();
    inputElement: HTMLInputElement;
    constructor(
        private changeDetector: ChangeDetectorRef,
        private elementRef: ElementRef,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService
    ) {}
    ngOnInit() {
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    ngAfterViewInit(): void {
        this.inputElement = this.elementRef.nativeElement.querySelector('input');
        this.setFocusToInput();
    }
    ngAfterViewChecked(): void {
        this.inputElement = this.elementRef.nativeElement.querySelector('input');
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.selectedTarget = target;
            if (target.isMerged === true) {
                this.isLink = target.isLink;
                if ( this.inputElement ) {
                    this.inputElement.value = '';
                }
                this.gatherTargets();
            }
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });
        this.userInputChange.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            takeUntil(this.unsubscribe)
        ).subscribe((res: string) => {
            const len = res.length;
            if ( len === 0 || len >= this.minLength ) {
                this.setFilterQuery(res);
            }
        });
    }
    isGroup(): boolean {
        return this.selectedTarget && this.selectedTarget.isMerged === true ? true : false;
    }
    gatherTargets(): void {
        if ( this.selectedTarget.isMerged ) {
            const targetList: any = [];
            if ( this.selectedTarget.isNode ) {
                this.selectedTarget.node.forEach(nodeKey => {
                    targetList.push([this.serverMapData.getNodeData(nodeKey), '']);
                });
            } else if ( this.selectedTarget.isLink ) {
                // Link 인 경우 필터 관련 버튼을 추가해야 함.
                this.selectedTarget.link.forEach(linkKey => {
                    targetList.push([this.serverMapData.getNodeData(this.serverMapData.getLinkData(linkKey).to), linkKey]);
                });
            }
            this.notFilteredTargetList = this.targetList = targetList;
        }
    }
    onSelectTarget(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NODE_IN_GROUPED_VIEW);
        this.storeHelperService.dispatch(new Actions.UpdateServerMapSelectedTargetByList(target[0]));
    }
    onOpenFilter(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_FILTER_TRANSACTION);
        const link = this.serverMapData.getLinkData(target[1]);
        this.urlRouteManagerService.openPage(this.urlRouteManagerService.makeFilterMapUrl({
            applicationName: link.filterApplicationName,
            serviceType: link.filterApplicationServiceTypeName,
            periodStr: this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithAddedWords(),
            timeStr: this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getEndTime(),
            filterStr: this.newUrlStateNotificationService.getPathValue(UrlPathId.FILTER),
            hintStr: this.newUrlStateNotificationService.getPathValue(UrlPathId.HINT),
            addedFilter: new Filter(
                link.sourceInfo.applicationName,
                link.sourceInfo.serviceType,
                link.targetInfo.applicationName,
                link.targetInfo.serviceType
            )}
        ));
    }
    getRequestSum(): number  {
        return this.targetList.reduce((accumulator: number,    target: any) => {
            return accumulator + target[0].totalCount;
        }, 0);
    }
    onOpenFilterWizard(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_FILTER_TRANSACTION_WIZARD);
        this.dynamicPopupService.openPopup({
            data: this.serverMapData.getLinkData(target[1]),
            component: FilterTransactionWizardPopupContainerComponent
        });
    }
    onKeyUp($event: any): void {
        if ( $event.keyCode === 27 ) {
            this.inputElement.value = '';
            this.setFilterQuery('');
        } else {
            this.userInputChange.next($event.target.value);
        }
    }
    setFocusToInput(): void {
        if ( this.inputElement ) {
            this.inputElement.focus();
        }
    }
    setFilterQuery(query: string): void {
        this.filterQuery = query;
        this.targetList = this.filterList();
        this.changeDetector.detectChanges();
    }
    filterList(): any[] {
        if ( this.filterQuery === '' ) {
            return this.notFilteredTargetList;
        }
        const filteredList: any = [];
        this.notFilteredTargetList.forEach(aTarget => {
            if ( aTarget[0].applicationName.indexOf(this.filterQuery) !== -1 ) {
                filteredList.push(aTarget);
            }
        });
        return filteredList;
    }
}
