import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ViewChild } from '@angular/core';
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
})
export class TargetListContainerComponent implements OnInit, OnDestroy {
    @ViewChild(SearchInputDirective, { static: true }) searchInputDirective: SearchInputDirective;

    private unsubscribe = new Subject<void>();

    i18nText: { [key: string]: string } = {};
    query = '';
    target: ISelectedTarget;
    minLength = 2;
    targetList: any[];
    serverMapData: ServerMapData;
    originalTargetList: any[];
    searchUseEnter = false;

    constructor(
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService
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
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });

        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => !!target)
        ).subscribe((target: ISelectedTarget) => {
            this.target = target;
            if (this.hasMultiInput()) {
                this.gatherTargets();
                this.initSearchInput();
            }
        });
    }

    private initSearchInput(): void {
        if (this.searchInputDirective) {
            this.searchInputDirective.clear();
        }
    }

    hasMultiInput(): boolean {
        if (this.target && !this.target.isWAS) {
            if (this.target.isMerged) {
                return true;
            } else {
                if (this.target.isLink) {
                    return false;
                } else {
                    const fromList = this.serverMapData.getLinkListByTo(this.target.node[0]);
                    if (fromList.length > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    gatherTargets(): void {
        const targetList: any[] = [];
        if (this.target.isNode) {
            this.target.node.forEach((nodeKey: string) => {
                targetList.push([this.serverMapData.getNodeData(nodeKey), '']);
            });
            if (this.target.groupedNode) {
                targetList.forEach((targetData: any[]) => {
                    targetData[0].fromList = this.target.groupedNode.map((key: string) => {
                        return this.serverMapData.getLinkData(key + '~' + targetData[0].key);
                    });
                });
            } else if (this.target.isMerged === false) {
                targetList[0][0].fromList = this.serverMapData.getLinkListByTo(this.target.node[0]);
            }
        } else if (this.target.isLink) {
            this.target.link.forEach((linkKey: string) => {
                targetList.push([this.serverMapData.getLinkData(linkKey), linkKey]);
            });
        }
        this.originalTargetList = this.targetList = targetList;
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
        return this.targetList.reduce((acc: number, target: any) => {
            return acc + target[0].totalCount;
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
        this.query = query;
        this.targetList = this.filterList();
    }

    filterList(): any[] {
        if ( this.query === '' ) {
            return this.originalTargetList;
        }
        const filteredList: any = [];
        this.originalTargetList.forEach((aTarget: any) => {
            if ( aTarget[0].applicationName.indexOf(this.query) !== -1 ) {
                filteredList.push(aTarget);
            }
        });
        return filteredList;
    }
}
