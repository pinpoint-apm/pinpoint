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
    }

    onChangeOption(optionKey: string): void {
        switch (optionKey) {
            case 'scatterScan':
                this.enableServerSideScanForScatter = !this.enableServerSideScanForScatter;
                this.webAppSettingDataService.setExperimentalOption('scatterScan', this.enableServerSideScanForScatter);
                break;
        }
    }
}
