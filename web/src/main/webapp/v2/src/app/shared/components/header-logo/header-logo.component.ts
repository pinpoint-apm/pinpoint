import { Component, OnInit } from '@angular/core';
import { WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-header-logo',
    templateUrl: './header-logo.component.html',
    styleUrls: ['./header-logo.component.css']
})
export class HeaderLogoComponent implements OnInit {
    logoPath: string;
    constructor(
        private webAppSettingDataService: WebAppSettingDataService
    ) { }

    ngOnInit() {
        this.logoPath = this.webAppSettingDataService.getLogoPath();
    }
}
