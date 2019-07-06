import { Component, OnInit, ComponentFactoryResolver, Injector } from '@angular/core';

import { TransactionViewTypeService, VIEW_TYPE, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-transaction-detail-contents-container',
    templateUrl: './transaction-detail-contents-container.component.html',
    styleUrls: ['./transaction-detail-contents-container.component.css']
})
export class TransactionDetailContentsContainerComponent implements OnInit {
    private currentViewType: string;
    constructor(
        private transactionViewTypeService: TransactionViewTypeService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.transactionViewTypeService.onChangeViewType$.subscribe((viewType: string) => {
            this.currentViewType = viewType;
        });
    }
    isHiddenSearchComponent(): boolean {
        return this.currentViewType !== VIEW_TYPE.CALL_TREE && this.currentViewType !== VIEW_TYPE.TIMELINE;
    }
    isSameType(type: string): boolean {
        return this.currentViewType === type;
    }
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.CALL_TREE);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.CALL_TREE,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
