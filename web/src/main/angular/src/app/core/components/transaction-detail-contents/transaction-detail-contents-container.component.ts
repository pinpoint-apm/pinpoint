import { Component, OnInit, ComponentFactoryResolver, Injector, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { Subject } from 'rxjs';
import { tap } from 'rxjs/operators';

import {
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    StoreHelperService,
} from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { CallTreeContainerComponent } from 'app/core/components/call-tree/call-tree-container.component';

@Component({
    selector: 'pp-transaction-detail-contents-container',
    templateUrl: './transaction-detail-contents-container.component.html',
    styleUrls: ['./transaction-detail-contents-container.component.css']
})
export class TransactionDetailContentsContainerComponent implements OnInit {
    @ViewChild(CallTreeContainerComponent, { read: ElementRef, static: true }) callTreeComponent: ElementRef;
    private unsubscribe = new Subject<void>();

    activeView: string;
    showSearch: boolean;

    constructor(
        private storeHelperService: StoreHelperService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.storeHelperService.getTransactionViewType(this.unsubscribe).pipe(
            tap((viewType: string) => {
                this.renderer.setStyle(this.callTreeComponent.nativeElement, 'display', viewType === 'callTree' ? 'block' : 'none');
            })
        ).subscribe((viewType: string) => {
            this.activeView = viewType;
            this.showSearch = this.activeView === 'callTree' || this.activeView === 'timeline';
        });
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
