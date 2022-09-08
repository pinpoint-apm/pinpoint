import { Component, OnInit, ComponentFactoryResolver, Injector, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil, tap } from 'rxjs/operators';

import { NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService, WebAppSettingDataService, MessageQueueService, MESSAGE_TO, UrlRouteManagerService } from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { InspectorPageService } from './inspector-page.service';

@Component({
    selector: 'pp-inspector-page',
    templateUrl: './inspector-page.component.html',
    styleUrls: ['./inspector-page.component.css'],
    providers: [InspectorPageService]
})
export class InspectorPageComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private funcImagePath: Function;

    unAuthImgPath: string;

    showSideMenu: boolean;
    isAccessDenyed$: Observable<boolean>;

    mainSectionStyle = {};
    isAppActivated: boolean;
    selectedAppImg: string;
    selectedAppName: string;

    constructor(
        private inspectorPageService: InspectorPageService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.unAuthImgPath = this.webAppSettingDataService.getServerMapIconPathMakeFunc()('UNAUTHORIZED');

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                if (urlService.hasValue(UrlPathId.APPLICATION)) {
                    const selectedApp: IApplication = urlService.getPathValue(UrlPathId.APPLICATION);

                    this.selectedAppName = selectedApp.getApplicationName();
                    this.selectedAppImg = this.funcImagePath(selectedApp.getServiceType());
                    this.isAppActivated = !urlService.hasValue(UrlPathId.AGENT_ID);
                }
            }),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.isRealTimeMode() || urlService.hasValue(UrlPathId.END_TIME);
            })
        ).subscribe((showSideMenu: boolean) => {
            this.showSideMenu = showSideMenu;
            this.mainSectionStyle = {
                width: showSideMenu ? 'calc(100% - 250px)' : '100%'
            };
        });
        this.isAccessDenyed$ = this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.IS_ACCESS_DENYED);
        this.inspectorPageService.activate(this.unsubscribe);
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectApp(): void {
        const url = this.newUrlStateNotificationService.isRealTimeMode() ?
            [
                UrlPath.INSPECTOR,
                UrlPath.REAL_TIME,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            ] :
            [
                UrlPath.INSPECTOR,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
            ];

        this.urlRouteManagerService.moveOnPage({url});
        // this.urlRouteManagerService.moveOnPage({
        //     url: [
        //         UrlPath.INSPECTOR,
        //         this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
        //         this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
        //         this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
        //     ]
        // })
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.NAVBAR);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.NAVBAR,
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
