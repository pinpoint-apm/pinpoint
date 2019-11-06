import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import agent from '@egjs/agent';

import { TranslateReplaceService } from 'app/shared/services/translate-replace.service';

@Injectable()
export class BrowserSupportCheckService {
    private userBrowserInfo: {name: string, version: string};
    // 2018.06.01 기준 지원하는 브라우저 최신버전
    private latestBrowserList = [
        {
            name: 'chrome',
            version: 66
        }, {
            name: 'firefox',
            version: 60
        }, {
            name: 'safari',
            version: 11
        }, {
            name: 'edge',
            version: 42
        }, {
            name: 'whale',
            version: 1
        }
    ];

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
    ) {
        const {browser: {name, version}} = agent();

        this.userBrowserInfo = {name, version};
    }

    private isBrowserSupported(): boolean {
        const {name, version} = this.userBrowserInfo;

        return this.latestBrowserList.findIndex((browserInfo) => {
            return browserInfo.name === name && browserInfo.version <= Number(version.split('.')[0]);
        }) !== -1;
    }

    getMessage(): Observable<string> {
        if (this.isBrowserSupported()) {
            return of('');
        } else {
            return this.translateService.get('SUPPORT.RESTRICT_USAGE').pipe(
                map(((message: string) => {
                    return this.translateReplaceService.replace(message, `${this.userBrowserInfo.name} ${this.userBrowserInfo.version}`);
                }))
            );
        }
    }
}
