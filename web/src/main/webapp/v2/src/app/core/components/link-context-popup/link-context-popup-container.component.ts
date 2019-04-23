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
import { UrlPathId } from 'app/shared/models';
import { FilterTransactionWizardPopupContainerComponent } from 'app/core/components/filter-transaction-wizard-popup/filter-transaction-wizard-popup-container.component';

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
        const isBothWas = this.data.sourceInfo.isWas && this.data.targetInfo.isWas;
        this.urlRouteManagerService.openPage(
            this.urlRouteManagerService.makeFilterMapUrl({
                applicationName: this.data.filterApplicationName,
                serviceType: this.data.filterApplicationServiceTypeName,
                periodStr: this.newUrlStateNotificationService.hasValue(UrlPathId.PERIOD) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime() : '',
                timeStr: this.newUrlStateNotificationService.hasValue(UrlPathId.END_TIME) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime() : '',
                filterStr: this.newUrlStateNotificationService.hasValue(UrlPathId.FILTER) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.FILTER) : '',
                hintStr: this.newUrlStateNotificationService.hasValue(UrlPathId.HINT) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.HINT) : '',
                addedFilter: new Filter(
                    this.data.sourceInfo.applicationName,
                    this.data.sourceInfo.serviceType,
                    this.data.targetInfo.applicationName,
                    this.data.targetInfo.serviceType
                ),
                addedHint: (isBothWas ? {
                    [this.data.targetInfo.applicationName]: this.data.filterTargetRpcList
                } : null)
            })
        );
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
