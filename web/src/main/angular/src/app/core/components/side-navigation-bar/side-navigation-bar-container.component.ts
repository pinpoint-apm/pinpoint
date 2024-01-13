import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';

import { WebAppSettingDataService,
    TRACKED_EVENT_LIST, UrlRouteManagerService, WindowRefService, AnalyticsService } from 'app/shared/services';
import { UrlPath } from 'app/shared/models';
import { TranslateService } from '@ngx-translate/core';

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
    i18nText: {[key: string]: string} = {
        FAVORITE_LIST_TITLE: '',
        CONFIGURATION_TITLE: '',
        USER_GROUP_TITLE: '',
        ALARM_TITLE: '',
        WEBHOOK_TITLE: '',
        INSTALLATION_TITLE: '',
        HELP_TITLE: '',
        EXPERIMENTAL_TITLE: '',
        ADMINISTRATION_TITLE: '',
        AGENT_STATISTIC_TITLE: '',
        AGENT_MANAGEMENT_TITLE: '',
        GENERAL_TITLE: '',
    };

    constructor(
        private cd: ChangeDetectorRef,
        private windowRefService: WindowRefService,
        private analyticsService: AnalyticsService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
    ) { }

    ngOnInit() {
        this.initI18nText();
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
    private initI18nText(): void {
        forkJoin(
            this.translateService.get('MAIN.FAVORITE_APP_LIST'),
            this.translateService.get('CONFIGURATION.TITLE'),
            this.translateService.get('CONFIGURATION.USER_GROUP.TITLE'),
            this.translateService.get('CONFIGURATION.ALARM.TITLE'),
            this.translateService.get('CONFIGURATION.WEBHOOK.TITLE'),
            this.translateService.get('CONFIGURATION.INSTALLATION.TITLE'),
            this.translateService.get('CONFIGURATION.HELP.TITLE'),
            this.translateService.get('CONFIGURATION.EXPERIMENTAL.TITLE'),
            this.translateService.get('CONFIGURATION.ADMINISTRATION.TITLE'),
            this.translateService.get('CONFIGURATION.AGENT_STATISTIC.TITLE'),
            this.translateService.get('CONFIGURATION.AGENT_MANAGEMENT.TITLE'),
            this.translateService.get('CONFIGURATION.GENERAL.TITLE')
        ).subscribe(([favoriteTitle, configurationTitle, userGroupTitle, alarmTitle, webhookTitle,
                         installationTitle, helpTitle, experimentalTitle,
                         administrationTitle, agentStatisticTitle, agentManagementTitle,
                         genralTitle]: string[]) => {
            this.i18nText.FAVORITE_LIST_TITLE = favoriteTitle;
            this.i18nText.CONFIGURATION_TITLE = configurationTitle;
            this.i18nText.USER_GROUP_TITLE = userGroupTitle;
            this.i18nText.ALARM_TITLE = alarmTitle;
            this.i18nText.WEBHOOK_TITLE = webhookTitle;
            this.i18nText.INSTALLATION_TITLE = installationTitle;
            this.i18nText.HELP_TITLE = helpTitle;
            this.i18nText.EXPERIMENTAL_TITLE = experimentalTitle;
            this.i18nText.ADMINISTRATION_TITLE = administrationTitle;
            this.i18nText.AGENT_STATISTIC_TITLE = agentStatisticTitle;
            this.i18nText.AGENT_MANAGEMENT_TITLE = agentManagementTitle;
            this.i18nText.GENERAL_TITLE = genralTitle;
        });
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
                    title: this.i18nText.CONFIGURATION_TITLE,
                    iconClass: 'fas fa-cog',
                    childItems: [
                        { title: this.i18nText.USER_GROUP_TITLE, id: 'userGroup', path: '/config/userGroup', },
                        { title: this.i18nText.ALARM_TITLE, id: 'alarm', path: '/config/alarm', },
                        { title: this.i18nText.WEBHOOK_TITLE, id: 'webhook', path: '/config/webhook', showItem: this.showWebhook, },
                        { title: this.i18nText.INSTALLATION_TITLE, id: 'installation', path: '/config/installation', },
                        // divider
                        { id: 'divider' },
                        { title: this.i18nText.HELP_TITLE, id: 'help', path: '/config/help', },
                        { title: 'Github', id: 'github', path: '', onClick: () => this.onClickGithubLink()},
                        // divider
                        { id: 'divider' },
                        { title: this.i18nText.EXPERIMENTAL_TITLE, id: 'experimental', path: '/config/experimental', },
                    ],
                },
                {
                    id: 'administration',
                    title: this.i18nText.ADMINISTRATION_TITLE,
                    iconClass: 'fas fa-user-cog',
                    childItems: [
                        { title: this.i18nText.AGENT_STATISTIC_TITLE, id: 'agentStatistic', path: '/config/agentStatistic', onClick: () => ''},
                        { title: this.i18nText.AGENT_MANAGEMENT_TITLE, id: 'agentmanagement', path: '/config/agentManagement', },
                    ]
                },
                {
                    id: 'user',
                    title: this.userId,
                    iconClass: 'fas fa-user-circle',
                    childItems: [
                        { title: this.i18nText.GENERAL_TITLE, id: 'general', path: '/config/general', },
                        { title: this.i18nText.FAVORITE_LIST_TITLE, id: 'favoriteList', path: '/config/favorite', },
                        // divider
                        { id: 'divider' },
                        { id: 'theme' },
                    ]
                },
            ]
        };
    }
}
