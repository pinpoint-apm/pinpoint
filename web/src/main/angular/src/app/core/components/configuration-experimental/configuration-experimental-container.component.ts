import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { AnalyticsService, ExperimentalConfiguration, ExperimentalConfigurationKeyType, 
    ExperimentalConfigurationLocalStorageKey, ExperimentalConfigurationMeta, 
    TRACKED_EVENT_LIST, UrlRouteManagerService, WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-experimental-container',
    templateUrl: 'configuration-experimental-container.component.html',
    styleUrls: ['./configuration-experimental-container.component.css'],
})
export class ConfigurationExperimentalContainerComponent implements OnInit {
    Object = Object;
    desc$: Observable<string>;

    configurationMeta: ExperimentalConfigurationMeta;
    configurations: ExperimentalConfiguration;

    constructor(
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private urlRouteManagerService: UrlRouteManagerService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.initDescText();
        this.setOptions();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.EXPERIMENTAL.DESC');
    }

    private setOptions() {
        this.webAppSettingDataService.getExperimentalConfiguration()
            .subscribe(configurationMeta => {
                this.configurationMeta = configurationMeta;
                const configurations = Object.keys(this.configurationMeta).reduce((prev, curr: ExperimentalConfigurationKeyType) => {
                    const localStorageKey = ExperimentalConfigurationLocalStorageKey[curr]
                    const optionFromLocalStorage = this.webAppSettingDataService.getExperimentalOption(localStorageKey);
                    
                    return { ...prev, [curr]: optionFromLocalStorage === null ? this.configurationMeta[curr].value : optionFromLocalStorage }
                }, {} as ExperimentalConfiguration);
        
                this.configurations = configurations;
            }
        );
    }

    onChangeOption(optionKey: ExperimentalConfigurationKeyType): void {
        const localStorageKey = ExperimentalConfigurationLocalStorageKey[optionKey];

        this.configurations =  { ...this.configurations, [optionKey]: !this.configurations[optionKey] };
        this.webAppSettingDataService.setExperimentalOption(localStorageKey, this.configurations[optionKey]);
        this.applyGA({optionKey, optionValue: this.configurations[optionKey]});

        if (optionKey === 'enableSideNavigationUI') {
            this.urlRouteManagerService.reload();
        }
    }

    private applyGA({optionKey, optionValue}: {optionKey: string, optionValue: boolean}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_EXPERIMENTAL_OPTION, `${optionValue ? 'Enable' : 'Disable'} ${optionKey}`);
    }
}
