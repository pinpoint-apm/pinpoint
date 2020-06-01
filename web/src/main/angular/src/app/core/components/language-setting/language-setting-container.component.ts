import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { WebAppSettingDataService, StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST, UrlRouteManagerService } from 'app/shared/services';

@Component({
    selector: 'pp-language-setting-container',
    templateUrl: './language-setting-container.component.html',
    styleUrls: ['./language-setting-container.component.css']
})
export class LanguageSettingContainerComponent implements OnInit {
    languageList = [
        {key: 'en', display: 'English'},
        {key: 'ko', display: '한국어'}
    ];

    currentLanguage$: Observable<string>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
        private analyticsService: AnalyticsService,
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}

    ngOnInit() {
        this.currentLanguage$ = this.storeHelperService.getLanguage();
    }

    onChangeLanguage(lang: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_LANGUAGE, lang);
        this.webAppSettingDataService.setLanguage(lang);
        this.urlRouteManagerService.reload();
    }
}
