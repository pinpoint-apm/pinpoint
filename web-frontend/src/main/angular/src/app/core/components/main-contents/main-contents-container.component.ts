import {Component, OnInit, ComponentFactoryResolver, Injector, ChangeDetectionStrategy} from '@angular/core';
import {Observable, combineLatest} from 'rxjs';
import {map} from 'rxjs/operators';

import {DynamicPopupService, NewUrlStateNotificationService, WebAppSettingDataService} from 'app/shared/services';
import {
    HELP_VIEWER_LIST,
    HelpViewerPopupContainerComponent
} from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import {UrlPathId} from 'app/shared/models';

@Component({
    selector: 'pp-main-contents-container',
    templateUrl: './main-contents-container.component.html',
    styleUrls: ['./main-contents-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainContentsContainerComponent implements OnInit {
    showElements$: Observable<boolean>;
    enableRealTime$: Observable<boolean>;

    isAppSelected$: Observable<boolean>;

    constructor(
        private dynamicPopupService: DynamicPopupService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {
    }

    ngOnInit() {
        this.showElements$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.PERIOD, UrlPathId.END_TIME))
        );

        this.enableRealTime$ = combineLatest(
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                map((urlService: NewUrlStateNotificationService) => urlService.isRealTimeMode())
            ),
            this.webAppSettingDataService.useActiveThreadChart()
        ).pipe(
            map(([isRealTimeMode, useActiveThreadChart]: boolean[]) => isRealTimeMode && useActiveThreadChart)
        );

        this.isAppSelected$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        );
    }

    onShowHelp($event: MouseEvent): void {
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.SERVER_MAP,
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
