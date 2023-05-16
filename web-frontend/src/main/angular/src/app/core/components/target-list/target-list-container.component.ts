import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin, merge } from 'rxjs';
import { tap, filter, takeUntil } from 'rxjs/operators';

import { UrlPathId, UrlQuery, UrlPath } from 'app/shared/models';
import { Filter } from 'app/core/models/filter';
import {
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { FilterTransactionWizardPopupContainerComponent } from 'app/core/components/filter-transaction-wizard-popup/filter-transaction-wizard-popup-container.component';
import { SearchInputDirective } from 'app/shared/directives/search-input.directive';
import { FilterParamMaker } from 'app/core/utils/filter-param-maker';
import { HintParamMaker } from 'app/core/utils/hint-param-maker';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-target-list-container',
    templateUrl: './target-list-container.component.html',
    styleUrls: ['./target-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TargetListContainerComponent implements OnInit, OnDestroy {
    @ViewChild(SearchInputDirective, {static: true}) searchInputDirective: SearchInputDirective;

    private unsubscribe = new Subject<void>();

    i18nText: { [key: string]: string } = {};
    target: ISelectedTarget;
    minLength = 2;
    targetList: any[];
    serverMapData: ServerMapData;
    originalTargetList: any[];
    searchUseEnter = false;
    showList = false;

    constructor(
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private messageQueueService: MessageQueueService,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.initI18NText();
        this.listenToEmitter();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18NText() {
        forkJoin(
            this.translateService.get('COMMON.SEARCH_INPUT'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe(([placeholder, emptyText]: string[]) => {
            this.i18nText.PLACE_HOLDER = placeholder;
            this.i18nText.EMPTY = emptyText;
        });
    }

    private listenToEmitter(): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil((this.unsubscribe)),
        ).subscribe(() => {
            this.serverMapData = null;
            this.target = null;
            this.originalTargetList = null;
            this.targetList = null;
        });

        merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).pipe(
                tap(({serverMapData}: {serverMapData: ServerMapData}) => this.serverMapData = serverMapData)
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).pipe(
                tap((target: ISelectedTarget) => this.target = target)
            )
        ).pipe(
            filter(() => !!this.serverMapData && !!this.target),
            filter(() => {
                if (this.target.isNode) {
                    const relatedNodeList = this.target.groupedNode ? [...this.target.groupedNode, ...this.target.node] : this.target.node;

                    return relatedNodeList.every((key: string) => !!this.serverMapData.getNodeData(key));
                } else {
                    const relatedLinkList = this.target.link;

                    return relatedLinkList.every((key: string) => !!this.serverMapData.getLinkData(key));
                }
            }),
        ).subscribe(() => {
            this.showList = this.hasMultiInput(this.target);
            this.originalTargetList = this.targetList = this.gatherTargets();
            this.initSearchInput();
            this.cd.detectChanges();
        });
    }

    private initSearchInput(): void {
        if (this.searchInputDirective) {
            this.searchInputDirective.clear();
        }
    }

    private hasMultiInput(target: ISelectedTarget): boolean {
        return target.isWAS ? false
            : target.isMerged ? true
            : target.isLink ? false
            : this.serverMapData.getLinkListByTo(target.node[0]).length > 1 ? true
            : false;
    }

    private gatherTargets(): any[] {
        return this.target.isLink ? this.target.link.map((key: string) => this.serverMapData.getLinkData(key))
            : this.target.node.map((key: string) => {
                const nodeData = this.serverMapData.getNodeData(key);

                return !this.target.isMerged ? {...nodeData, fromList: this.serverMapData.getLinkListByTo(this.target.node[0])}
                    : this.target.groupedNode ? {...nodeData, fromList: this.target.groupedNode.map((sourceNodeKey: string) => {
                        return this.serverMapData.getLinkData(`${sourceNodeKey}~${key}`);
                    })}
                    : nodeData;
            });
    }

    onSelectTarget(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NODE_IN_GROUPED_VIEW);
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SERVER_MAP_TARGET_SELECT_BY_LIST,
            param: target
        });
    }

    onOpenFilter(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_FILTERED_MAP_PAGE_ON_MERGED_TARGET_LIST);
        const appKey = `${target.filterApplicationName}@${target.filterApplicationServiceTypeName}`;
        const period = this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithAddedWords();
        const endTime = this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime();
        const currFilterStr = this.newUrlStateNotificationService.hasValue(UrlQuery.FILTER) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.FILTER) : '';
        const addedFilter = new Filter(
            target.sourceInfo.applicationName,
            target.sourceInfo.serviceType,
            target.targetInfo.applicationName,
            target.targetInfo.serviceType
        );
        const currHintStr = this.newUrlStateNotificationService.hasValue(UrlQuery.HINT) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.HINT) : '';
        const addedHint = target.sourceInfo.isWas && target.targetInfo.isWas ? {[target.targetInfo.applicationName]: target.filterTargetRpcList} : null;

        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.FILTERED_MAP,
                appKey,
                period,
                endTime
            ],
            queryParams: {
                filter: FilterParamMaker.makeParam(currFilterStr, addedFilter),
                hint: HintParamMaker.makeParam(currHintStr, addedHint)
            }
        });
    }

    getRequestSum(): number  {
        return this.targetList.reduce((acc: number, target: any) => acc + target.totalCount, 0);
    }

    onOpenFilterWizard(target: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_FILTER_TRANSACTION_WIZARD_POPUP_ON_MERGED_TARGET_LIST);
        this.dynamicPopupService.openPopup({
            data: target,
            component: FilterTransactionWizardPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    isEmpty(): boolean {
        return isEmpty(this.targetList);
    }

    onCancel(): void {
        this.setFilterQuery('');
    }

    onSearch(query: string): void {
        this.setFilterQuery(query);
    }

    setFilterQuery(query: string): void {
        this.targetList = this.filterList(query);
    }

    private filterList(query: string): any[] {
        if (query === '') {
            return this.originalTargetList;
        }

        return this.target.isLink ? this.originalTargetList.filter(({targetInfo: {applicationName}}) => applicationName.includes(query))
            : this.originalTargetList.filter(({applicationName}) => applicationName.includes(query));
    }
}
