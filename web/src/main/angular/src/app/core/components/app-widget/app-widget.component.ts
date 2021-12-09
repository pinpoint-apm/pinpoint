import { Component, ComponentFactoryResolver, Injector, Input, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, WebAppSettingDataService, AnalyticsService, DynamicPopupService, UrlRouteManagerService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { Subject, Observable, fromEvent } from 'rxjs';
import { map, takeUntil, pluck, startWith, delay, filter } from 'rxjs/operators';
import { TransactionIdSearchContainerComponent } from 'app/core/components/transaction-id-search/transaction-id-search-container.component';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

const enum ScreenWidth {
    MIN = 1380
}

@Component({
    selector: 'pp-app-widget',
    templateUrl: './app-widget.component.html',
    styleUrls: ['./app-widget.component.css']
})
export class AppWidgetComponent implements OnInit {
    @Input() showServermapOption = true;
    @Input() showTransactionIdSearch = true;
    private unsubscribe = new Subject<void>();
    private guideText: string;

    isAppSelected$: Observable<boolean>;
    isScreenWideEnough$: Observable<boolean>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.translateService.get('MAIN.VISIBILITY_HIDDEN').subscribe((text: string) => {
            this.guideText = text;
        });
        this.isAppSelected$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        );
        this.webAppSettingDataService.getVersion().subscribe((version: string) => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.VERSION, version);
        });

        this.addEventListener();
    }

    private addEventListener(): void {
        this.isScreenWideEnough$ = fromEvent(window, 'resize').pipe(
            takeUntil(this.unsubscribe),
            pluck('target', 'innerWidth'),
            startWith(window.innerWidth),
            map((width: number) => width >= ScreenWidth.MIN)
        );

        fromEvent(document, 'visibilitychange').pipe(
            takeUntil(this.unsubscribe),
            filter(() => document.hidden),
            filter(() => this.newUrlStateNotificationService.isRealTimeMode()),
            delay(10000),
            filter(() => document.hidden),
        ).subscribe(() => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Notice',
                    contents: this.guideText,
                    type: 'html'
                },
                component: MessagePopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.reload();
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onClickSearchIcon(btn: HTMLElement): void {
        const {left, top, width, height} = btn.getBoundingClientRect();
        const coord = {
            coordX: left + width / 2,
            coordY: top + height / 2
        };

        this.dynamicPopupService.openPopup({
            coord,
            component: TransactionIdSearchContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
