import { Component, OnInit, ChangeDetectionStrategy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import {
    WebAppSettingDataService,
    GutterEventService,
    NewUrlStateNotificationService,
    DynamicPopupService,
    UrlRouteManagerService
} from 'app/shared/services';
import { UrlPathId, UrlPath, UrlQuery } from 'app/shared/models';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

@Component({
    selector: 'pp-transaction-list-page',
    templateUrl: './transaction-list-page.component.html',
    styleUrls: ['./transaction-list-page.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionListPageComponent implements OnInit {
    private errorMessage: string;

    sideNavigationUI: boolean;

    splitSize: number[];

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private gutterEventService: GutterEventService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
    ) {}

    ngOnInit() {
        this.sideNavigationUI = this.webAppSettingDataService.getExperimentalOption('sideNavigationUI');
        
        this.translateService.get('TRANSACTION_LIST.TRANSACTION_RETRIEVE_ERROR').subscribe((text: string) => {
            this.errorMessage = text;
        });

        if (!this.newUrlStateNotificationService.hasValue(UrlQuery.DRAG_INFO)) {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Notice',
                    contents: this.errorMessage,
                    type: 'html'
                },
                component: MessagePopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.moveOnPage({
                        url: [
                            UrlPath.MAIN,
                            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                        ],
                        queryParams: {
                            [UrlQuery.DRAG_INFO]: null,
                            [UrlQuery.TRANSACTION_INFO]: null
                        }
                    });
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        }

        this.splitSize = this.webAppSettingDataService.getSplitSize();
    }

    onGutterResized({sizes}: {sizes: number[]}): void {
        this.webAppSettingDataService.setSplitSize(sizes.map((size: number): number => {
            return Number.parseFloat(size.toFixed(2));
        }));
        this.gutterEventService.resizedGutter(sizes);
    }
}
