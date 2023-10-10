import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';

import { WebAppSettingDataService, 
    TRACKED_EVENT_LIST, UrlRouteManagerService, WindowRefService, AnalyticsService } from 'app/shared/services';
import { UrlPath } from 'app/shared/models';

export interface ISNBItem {
    id: string;
    title?: string;
    path?: string;
    class?: string;
    iconClass?: string;
    disable?: () => boolean;
    onClick?: (item: ISNBItem) => void;
    childItems?: ISNBItem[];
    showItem?: boolean;
}

interface ISNBMeta {
    topItems: ISNBItem[];
    bottomItems: ISNBItem[];
}

@Component({
    selector: 'pp-side-navigation-bar',
    templateUrl: './side-navigation-bar-container.component.html',
    styleUrls: ['./side-navigation-bar-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideNavigationBarContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    private showMetric: boolean;
    private showUrlStat: boolean;
    private showWebhook: boolean;

    logoPath: string;
    minimize = false;
    currentItemId: string;
    userId = '';
    meta: ISNBMeta;

    constructor(
        private cd: ChangeDetectorRef,
        private windowRefService: WindowRefService,
        private analyticsService: AnalyticsService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) { }

    ngOnInit() {
        this.minimize = this.webAppSettingDataService.getSideNavBarScale();
        this.webAppSettingDataService.showMetric().subscribe((showMetric: boolean) => {
            this.showMetric = showMetric;
        });

        this.webAppSettingDataService.showUrlStat().subscribe((showUrlStat: boolean) => {
            this.showUrlStat = showUrlStat;
        });

        this.webAppSettingDataService.isWebhookEnable().subscribe((webhookEnable: boolean) => {
            this.showWebhook = webhookEnable;
        });


        this.webAppSettingDataService.getUserId().subscribe((userId: string) => {
            this.userId = userId || 'dev';
            this.meta = this.generatNavItemMeta();
        });
        
        this.logoPath = this.webAppSettingDataService.getLogoPath(this.minimize);

        this.cd.detectChanges();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onClickSizeScale(): void {
        this.minimize = !this.minimize;
        this.webAppSettingDataService.setSideNavBarScale(this.minimize);
        this.logoPath = this.webAppSettingDataService.getLogoPath(this.minimize);
    }

    onClickServermap(): void {
        this.urlRouteManagerService.moveToAppMenu(UrlPath.MAIN);
    }

    onClickInspector(): void {
        this.urlRouteManagerService.moveToAppMenu(UrlPath.INSPECTOR);
    }

    onClickMetric(): void {
        this.urlRouteManagerService.moveToHostMenu(UrlPath.METRIC);
    }

    onClickUrlStat(): void {
        this.urlRouteManagerService.moveToAppMenu(UrlPath.URL_STATISTIC);
    }

    onClickGithubLink() {
       this.windowRefService.nativeWindow.open('http://github.com/naver/pinpoint');
    }

    onLogoClick(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_LOGO_BUTTON);
    }

    getUserId() {
        return this.userId;
    }

    generatNavItemMeta(): ISNBMeta {
        return {
            topItems: [
                {
                    id: 'servermap',
                    title: 'Servermap',
                    path: '/main',
                    iconClass: 'fa fa-network-wired',
                    onClick: () => this.onClickServermap(),
                },
                {
                    id: 'inspector',
                    title: 'Inspector',
                    path: '/inspector',
                    iconClass: 'fas fa-chart-line',
                    onClick: () => this.onClickInspector(),
                },
                {
                    id: 'url-stat',
                    title: 'URL Statistic',
                    path: '/urlStatistic',
                    iconClass: 'fas fa-chart-bar',
                    showItem: this.showUrlStat,
                    onClick: () => this.onClickUrlStat(),
                },
                {
                    id: 'infrastructure',
                    title: 'Infrastructure',
                    path: '/metric',
                    iconClass: 'fas fa-server',
                    showItem: this.showMetric,
                    onClick: () => this.onClickMetric(),
                }
            ],
            bottomItems: [
                {
                    id: 'configuration',
                    title: 'Configuration',
                    iconClass: 'fas fa-cog',
                    childItems: [
                        { title: 'User Group', id: 'userGroup', path: '/config/userGroup', },
                        { title: 'Alarm', id: 'alarm', path: '/config/alarm', },
                        { title: 'Webhook', id: 'webhook', path: '/config/webhook', showItem: this.showWebhook, },
                        { title: 'Installation', id: 'installation', path: '/config/installation', },
                        // divider
                        { id: 'divider' },
                        { title: 'Help', id: 'help', path: '/config/help', },
                        { title: 'Github', id: 'github', path: '', onClick: () => this.onClickGithubLink()},
                        // divider
                        { id: 'divider' },
                        { title: 'Experimental', id: 'experimental', path: '/config/experimental', },
                    ],
                },
                {
                    id: 'administration',
                    title: 'Administration',
                    iconClass: 'fas fa-user-cog',
                    childItems: [
                        { title: 'Agent Statistic', id: 'agentStatistic', path: '/config/agentStatistic', onClick: () => ''},
                        { title: 'Agent management', id: 'agentmanagement', path: '/config/agentManagement', },
                    ]
                },
                {
                    id: 'user',
                    title: this.userId,
                    iconClass: 'fas fa-user-circle',
                    childItems: [
                        { title: 'General', id: 'general', path: '/config/general', },
                        { title: 'Favorite List', id: 'favoriteList', path: '/config/favorite', },
                        // divider
                        { id: 'divider' },
                        { id: 'theme' },
                    ]
                },
            ]
        };
    }
}
