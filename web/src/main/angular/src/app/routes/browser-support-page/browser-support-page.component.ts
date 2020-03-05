import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import agent from '@egjs/agent';

import { WebAppSettingDataService } from 'app/shared/services';

interface IBrowserInfo {
    downloadLink: string;
    name: string;
    displayName: string;
}

@Component({
    selector: 'pp-browser-support-page',
    templateUrl: './browser-support-page.component.html',
    styleUrls: ['./browser-support-page.component.css']
})
export class BrowserSupportPageComponent implements OnInit {
    private browserInfoList: IBrowserInfo[];
    private userAgentInfo: {[key: string]: any};

    funcImagePath: Function;
    i18nText$: Observable<string>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        this.userAgentInfo = agent();
        this.browserInfoList = [
            {
                downloadLink: 'https://www.google.com/chrome',
                name: 'chrome',
                displayName: 'Google Chrome'
            }, {
                downloadLink: 'https://www.mozilla.org/en/firefox/new',
                name: 'firefox',
                displayName: 'Mozilla Firefox'
            }, {
                downloadLink: 'https://support.apple.com/en-us/HT204416',
                name: 'safari',
                displayName: 'Apple Safari'
            }, {
                downloadLink: 'https://www.microsoft.com/en-us/windows/microsoft-edge',
                name: 'edge',
                displayName: 'Microsoft Edge'
            }, {
                downloadLink: 'https://whale.naver.com/en/',
                name: 'whale',
                displayName: 'Naver Whale'
            }
        ];
        this.i18nText$ = this.translateService.get('SUPPORT.INSTALL_GUIDE');
    }

    getFilteredBrowserInfoList(): IBrowserInfo[] {
        const userOSName = this.userAgentInfo.os.name;

        return this.browserInfoList.filter((browserInfo: IBrowserInfo) => {
            switch (userOSName) {
                case 'window':
                    return browserInfo.name !== 'safari';
                case 'mac':
                    return browserInfo.name !== 'edge';
                default:
                    return browserInfo.name === 'chrome' || browserInfo.name === 'firefox';
            }
        });
    }
}
