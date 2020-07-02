import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit, ComponentFactoryResolver, Injector } from '@angular/core';

import {
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    DynamicPopup
} from 'app/shared/services';
import { FilterAppTransactionWizardPopupContainerComponent} from 'app/core/components/filter-app-transaction-wizard-popup/filter-app-transaction-wizard-popup-container.component';

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
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_FILTER_TRANSACTION_WIZARD_POPUP_ON_NODE_CONTEXT_POPUP);
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
