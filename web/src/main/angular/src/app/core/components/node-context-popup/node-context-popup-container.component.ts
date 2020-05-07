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
import { FilterAppTransactionWizardPopupContainerComponent} from "../filter-app-transaction-wizard-popup/filter-app-transaction-wizard-popup-container.component";
import { FilterParamMaker } from 'app/core/utils/filter-param-maker';
import { HintParamMaker } from 'app/core/utils/hint-param-maker';

@Component({
    selector: 'pp-node-context-popup-container',
    templateUrl: './node-context-popup-container.component.html',
    styleUrls: ['./node-context-popup-container.component.css'],
})
export class NodeContextPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
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

    onClickApplicationFilterTransactionWizard(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_FILTER_TRANSACTION_WIZARD);
        this.dynamicPopupService.openPopup({
            data: this.data,
            component: FilterAppTransactionWizardPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
