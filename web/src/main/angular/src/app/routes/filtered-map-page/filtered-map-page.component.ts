import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-filtered-map-page',
    templateUrl: './filtered-map-page.component.html',
    styleUrls: ['./filtered-map-page.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FilteredMapPageComponent implements OnInit {
    sideNavigationUI: boolean;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}
    ngOnInit() {
        this.sideNavigationUI = this.webAppSettingDataService.getExperimentalOption('sideNavigationUI');
    }
}
