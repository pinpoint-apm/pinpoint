import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { WebAppSettingDataService } from 'app/shared/services';

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

    constructor(
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.initDescText();
        this.setOptions();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.EXPERIMENTAL.DESC');
    }

    private setOptions(): void {
        this.enableServerSideScanForScatter = this.webAppSettingDataService.getExperimentalOption('scatterScan');
        this.useStatisticsAgentState = this.webAppSettingDataService.getExperimentalOption('statisticsAgentState');
        this.enableServerMapRealTime = this.webAppSettingDataService.getExperimentalOption('serverMapRealTime');
    }

    onChangeOption(optionKey: string): void {
        switch (optionKey) {
            case 'scatterScan':
                this.enableServerSideScanForScatter = !this.enableServerSideScanForScatter;
                this.webAppSettingDataService.setExperimentalOption('scatterScan', this.enableServerSideScanForScatter);
                break;
            case 'statisticsAgentState':
                this.useStatisticsAgentState = !this.useStatisticsAgentState;
                this.webAppSettingDataService.setExperimentalOption('statisticsAgentState', this.useStatisticsAgentState);
                break;
            case 'serverMapRealTime':
                this.enableServerMapRealTime = !this.enableServerMapRealTime;
                this.webAppSettingDataService.setExperimentalOption('serverMapRealTime', this.enableServerMapRealTime);
                break;
        }
    }
}
