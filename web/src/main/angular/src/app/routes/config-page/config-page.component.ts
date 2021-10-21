import { Component, OnInit } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

import { UrlRouteManagerService, NewUrlStateNotificationService, TRACKED_EVENT_LIST, AnalyticsService, WebAppSettingDataService } from 'app/shared/services';
import { UrlPath } from 'app/shared/models';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css'],
    animations: [
        trigger('collapseSpread', [
            state('collapsed', style({
                maxHeight: 0,
                overflow: 'hidden'
            })),
            state('spreaded', style({
                maxHeight: '300px'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.3s')
            ])
        ]),
        trigger('rightDown', [
            state('collapsed', style({
                transform: 'none'
            })),
            state('spreaded', style({
                transform: 'rotate(90deg)'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.1s')
            ])
        ])
    ]
})
export class ConfigPageComponent implements OnInit {
    isMenuCollapsed: {[key: string]: boolean} = {
        admin: false,
        setting: false
    };
    webhookEnable: boolean;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initWebhookConfig();
    }

    private initWebhookConfig(): void {
        this.webAppSettingDataService.isWebhookEnable().subscribe((webhookEnable: boolean) => {
            this.webhookEnable = webhookEnable
        });
    }

    onClickExit(): void {
        const {startPath, pathParams, queryParams: query} = this.newUrlStateNotificationService.getPrevPageUrlInfo();
        const url = startPath === UrlPath.CONFIG ? [UrlPath.MAIN] : [startPath, ...pathParams.values()];
        const queryParams = [...query.entries()].reduce((acc: object, [key, value]: string[]) => {
            return { ...acc, [key]: value };
        }, {});

        this.urlRouteManagerService.moveOnPage({url, queryParams});
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_PAGE_EXIT_BUTTON);
    }

    toggleMenu(menu: string): void {
        this.isMenuCollapsed[menu] = !this.isMenuCollapsed[menu];
    }

    getCollapsedState(menu: string): string {
        return this.isMenuCollapsed[menu] ? 'collapsed' : 'spreaded';
    }

    isActive(linkElement: HTMLAnchorElement): boolean {
        const listItem = linkElement.parentElement;

        return Array.from(listItem.nextElementSibling.querySelectorAll('.l-link')).some((element: HTMLElement) => {
            return element.classList.contains('active');
        });
    }

    onMenuClick(menu: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_MENU, menu);
    }
}
