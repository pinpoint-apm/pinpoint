import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, defer, from } from 'rxjs';
import { getAccurateAgent } from '@egjs/agent';
import { map, pluck } from 'rxjs/operators';

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
    private browserInfoList: IBrowserInfo[] = [
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
    
    funcImagePath: Function;
    i18nText$: Observable<string>;
    filteredBrowserInfoList$: Observable<IBrowserInfo[]>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService
    ) {}

    ngOnInit() {
        this.filteredBrowserInfoList$ = defer(() => from(getAccurateAgent())).pipe(
            pluck('os', 'name'),
            map((userOSName: string) => {
                return this.browserInfoList.filter(({name:browserName}: IBrowserInfo) => {
                    switch (userOSName) {
                        case 'window':
                            return browserName !== 'safari';
                        case 'macos':
                            return browserName !== 'edge';
                        default:
                            return browserName === 'chrome' || browserName === 'firefox';
                    }
                });
            })
        );

        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        this.i18nText$ = this.translateService.get('SUPPORT.INSTALL_GUIDE');
    }
}
