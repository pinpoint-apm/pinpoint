import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { AnalyticsService, TRACKED_EVENT_LIST, WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-experimental-container',
    templateUrl: 'configuration-experimental-container.component.html',
    styleUrls: ['./configuration-experimental-container.component.css'],
})
export class ConfigurationExperimentalContainerComponent implements OnInit {
    desc$: Observable<string>;

    enableServerSideScanForScatter: boolean;
    useStatisticsAgentState: boolean;
    enableServerMapRealTime: boolean;
    sampleScatter: boolean;

    constructor(
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initDescText();
        this.setOptions();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.EXPERIMENTAL.DESC');
    }

    private setOptions(): void {
        // TODO: Set it as key-value object?
        const enableServerSideScanForScatter = this.webAppSettingDataService.getExperimentalOption('scatterScan');
        const useStatisticsAgentState = this.webAppSettingDataService.getExperimentalOption('statisticsAgentState');
        const enableServerMapRealTime = this.webAppSettingDataService.getExperimentalOption('serverMapRealTime');
        const sampleScatter = this.webAppSettingDataService.getExperimentalOption('scatterSampling');

        this.enableServerSideScanForScatter = enableServerSideScanForScatter === null ? true : enableServerSideScanForScatter;
        this.useStatisticsAgentState = useStatisticsAgentState === null ? true : useStatisticsAgentState;
        this.enableServerMapRealTime = enableServerMapRealTime === null ? true : enableServerMapRealTime;
        this.sampleScatter = sampleScatter === null ? true : sampleScatter;
    }

    onChangeOption(optionKey: string): void {
        switch (optionKey) {
            case 'scatterScan':
                this.enableServerSideScanForScatter = !this.enableServerSideScanForScatter;
                this.webAppSettingDataService.setExperimentalOption('scatterScan', this.enableServerSideScanForScatter);
                this.applyGA({optionKey, optionValue: this.enableServerSideScanForScatter});
                break;
            case 'statisticsAgentState':
                this.useStatisticsAgentState = !this.useStatisticsAgentState;
                this.webAppSettingDataService.setExperimentalOption('statisticsAgentState', this.useStatisticsAgentState);
                this.applyGA({optionKey, optionValue: this.useStatisticsAgentState});
                break;
            case 'serverMapRealTime':
                this.enableServerMapRealTime = !this.enableServerMapRealTime;
                this.webAppSettingDataService.setExperimentalOption('serverMapRealTime', this.enableServerMapRealTime);
                this.applyGA({optionKey, optionValue: this.enableServerMapRealTime});
                break;
            case 'scatterSampling':
                this.sampleScatter = !this.sampleScatter;
                this.webAppSettingDataService.setExperimentalOption('scatterSampling', this.sampleScatter);
                this.applyGA({optionKey, optionValue: this.sampleScatter});
                break;
        }
    }

    private applyGA({optionKey, optionValue}: {optionKey: string, optionValue: boolean}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_EXPERIMENTAL_OPTION, `${optionValue ? 'Enable' : 'Disable'} ${optionKey}`);
    }
}
