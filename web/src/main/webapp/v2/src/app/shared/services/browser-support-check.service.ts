import { Injectable } from '@angular/core';
import * as bowser from 'bowser';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { TranslateReplaceService } from 'app/shared/services/translate-replace.service';

@Injectable()
export class BrowserSupportCheckService {
    // 2018.06.01 기준 지원하는 브라우저 최신버전
    private latestBrowserList = [
        {
            name: 'Chrome',
            version: 66
        }, {
            name: 'Firefox',
            version: 60
        }, {
            name: 'Safari',
            version: 11
        }, {
            name: 'Microsoft Edge',
            version: 42
        }, {
            name: 'NAVER Whale browser',
            version: 1
        }
    ];

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
    ) {}

    private isBrowserSupported(): boolean {
        const userBrowserInfo = {
            name: bowser.name,
            version: Number((bowser.version as string).split('.')[0])
        };

        return this.latestBrowserList.findIndex((browserInfo) => {
            return browserInfo.name === userBrowserInfo.name && browserInfo.version <= userBrowserInfo.version;
        }) !== -1;
    }

    getMessage(): Observable<string> {
        if (this.isBrowserSupported()) {
            return of('');
        } else {
            return this.translateService.get('SUPPORT.RESTRICT_USAGE').pipe(
                map(((message: string) => {
                    return this.translateReplaceService.replace(message, bowser.name + ' ' + bowser.version);
                }))
            );
        }
    }
}
