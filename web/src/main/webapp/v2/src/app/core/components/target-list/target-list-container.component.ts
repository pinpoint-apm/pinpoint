import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ComponentFactoryResolver, Injector, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';

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
import { SearchInputDirective } from 'app/shared/directives/search-input.directive';

@Component({
    selector: 'pp-target-list-container',
    templateUrl: './target-list-container.component.html',
    styleUrls: ['./target-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TargetListContainerComponent implements OnInit, OnDestroy {
    @ViewChild(SearchInputDirective) searchInputDirective: SearchInputDirective;
    i18nText: { [key: string]: string } = {};
    minLength = 2;
    isLink = false;
    filterQuery = '';
    selectedTarget: any;
    serverMapData: ServerMapData;
    notFilteredTargetList: any[];
    targetList: any[];
    unsubscribe: Subject<null> = new Subject();
    searchUseEnter = false;
    constructor(
        private changeDetector: ChangeDetectorRef,
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}
    ngOnInit() {
        this.getI18NText();
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText() {
        this.translateService.get('COMMON.SEARCH_INPUT').subscribe((txt: string) => {
            this.i18nText.PLACE_HOLDER = txt;
        });
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.selectedTarget = target;
            if (this.hasMultiInput()) {
                this.isLink = target.isLink;
                if (this.searchInputDirective) {
                    this.searchInputDirective.clear();
                }
                this.gatherTargets();
            }
            this.changeDetector.detectChanges();
        });
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });
    }
    hasMultiInput(): boolean {
        if (this.selectedTarget && this.selectedTarget.isWAS === false) {
            if (this.selectedTarget.isMerged) {
                return true;
            } else {
                // if (this.selectedTarget.isLink) {
                //     return false;
                // } else {
                //     const fromList = this.serverMapData.getLinkListByTo(this.selectedTarget.node[0]);
                //     if (fromList.length > 1) {
                //         return true;
                //     }
                // }
            }
        }
        return false;
    }
    gatherTargets(): void {
        const targetList: any[] = [];
        if (this.selectedTarget.isNode) {
            this.selectedTarget.node.forEach((nodeKey: string) => {
                targetList.push([this.serverMapData.getNodeData(nodeKey), '']);
            });
            if (this.selectedTarget.groupedNode) {
                targetList.forEach((targetData: any[]) => {
                    targetData[0].fromList = this.selectedTarget.groupedNode.map((key: string) => {
                        return this.serverMapData.getLinkData(key + '~' + targetData[0].key);
                    });
                });
            } else if (this.selectedTarget.isMerged === false) {
                targetList[0][0].fromList = this.serverMapData.getLinkListByTo(this.selectedTarget.node[0]);
            }
        } else if (this.selectedTarget.isLink) {
            // Link 인 경우 필터 관련 버튼을 추가해야 함.
            this.selectedTarget.link.forEach((linkKey: string) => {
                targetList.push([this.serverMapData.getLinkData(linkKey), linkKey]);
            });
        }
        this.notFilteredTargetList = this.targetList = targetList;
    }
    onSelectTarget(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NODE_IN_GROUPED_VIEW);
        this.storeHelperService.dispatch(new Actions.UpdateServerMapSelectedTargetByList(target));
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
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
    onCancel(): void {
        this.setFilterQuery('');
    }
    onSearch(query: string): void {
        this.setFilterQuery(query);
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
