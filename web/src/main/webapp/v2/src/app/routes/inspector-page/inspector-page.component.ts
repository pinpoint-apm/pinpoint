import { Component, OnInit } from '@angular/core';
import { state, style, animate, transition, trigger } from '@angular/animations';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { RouteInfoCollectorService, NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { EndTime } from 'app/core/models';
import { UrlPathId } from 'app/shared/models';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-inspector-page',
    animations: [
        trigger('fadeInOut', [
            state('in', style({transform: 'translateX(0)', opacity: 1})),
            transition(':leave', [ // is alias to '* => void'
                animate('1.5s 0.1s ease-in', style({
                    transform: 'translateX(-100%)',
                    opacity: 0
                }))
            ]),
            transition(':enter', [ // is alias to 'void => *'
                style({
                    transform: 'translateX(-100%)',
                    opacity: 0
                }),
                animate('2s ease-out')
            ])
        ])
    ],
    templateUrl: './inspector-page.component.html',
    styleUrls: ['./inspector-page.component.css']
})
export class InspectorPageComponent implements OnInit {
    endTime$: Observable<EndTime>;

    constructor(
        private routeInfoCollectorService: RouteInfoCollectorService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService
    ) {}
    ngOnInit() {
        this.endTime$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.getPathValue(UrlPathId.END_TIME);
            })
        );
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
        });
    }
}
