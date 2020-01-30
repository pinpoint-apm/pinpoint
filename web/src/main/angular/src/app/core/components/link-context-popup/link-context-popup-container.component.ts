import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit, ComponentFactoryResolver, Injector } from '@angular/core';

import {
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    DynamicPopup
} from 'app/shared/services';
import { Filter } from 'app/core/models/filter';
import { UrlPathId, UrlPath, UrlQuery } from 'app/shared/models';
import { FilterTransactionWizardPopupContainerComponent } from 'app/core/components/filter-transaction-wizard-popup/filter-transaction-wizard-popup-container.component';
import { FilterParamMaker } from 'app/core/utils/filter-param-maker';
import { HintParamMaker } from 'app/core/utils/hint-param-maker';

@Component({
    selector: 'pp-link-context-popup-container',
    templateUrl: './link-context-popup-container.component.html',
    styleUrls: ['./link-context-popup-container.component.css'],
})
export class LinkContextPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: any;
    @Input() coord: ICoordinate;
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outClose = new EventEmitter<void>();

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {}
    ngAfterViewInit() {
        this.outCreated.emit(this.coord);
    }

    onInputChange({coord}: {coord: ICoordinate}): void {
        this.outCreated.emit(coord);
    }

    onClickFilterTransaction(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_FILTER_TRANSACTION);
        this.outClose.emit();
        const appKey = `${this.data.filterApplicationName}@${this.data.filterApplicationServiceTypeName}`;
        const period = this.newUrlStateNotificationService.hasValue(UrlPathId.PERIOD) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime() : '';
        const endTime = this.newUrlStateNotificationService.hasValue(UrlPathId.END_TIME) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime() : '';
        const currFilterStr = this.newUrlStateNotificationService.hasValue(UrlQuery.FILTER) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.FILTER) : '';
        const addedFilter = new Filter(
            this.data.sourceInfo.applicationName,
            this.data.sourceInfo.serviceType,
            this.data.targetInfo.applicationName,
            this.data.targetInfo.serviceType
        );
        const currHintStr = this.newUrlStateNotificationService.hasValue(UrlQuery.HINT) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.HINT) : '';
        const addedHint = this.data.sourceInfo.isWas && this.data.targetInfo.isWas ? {[this.data.targetInfo.applicationName]: this.data.filterTargetRpcList} : null;

        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.FILTERED_MAP,
                appKey,
                period,
                endTime
            ],
            queryParam: {
                filter: FilterParamMaker.makeParam(currFilterStr, addedFilter),
                hint: HintParamMaker.makeParam(currHintStr, addedHint)
            }
        });
    }

    onClickFilterTransactionWizard(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_FILTER_TRANSACTION_WIZARD);
        this.dynamicPopupService.openPopup({
            data: this.data,
            component: FilterTransactionWizardPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
