import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { WebAppSettingDataService, UrlRouteManagerService } from 'app/shared/services';

@Component({
    selector: 'pp-url-redirector',
    template: ''
})
export class UrlRedirectorComponent {
    constructor(
        private activatedRoute: ActivatedRoute,
        private webAppSettingDataService: WebAppSettingDataService,
        private urlRouteManagerService: UrlRouteManagerService,
    ) {
        // @ALERT
        // URL Redirector가 호출되는 상황은 RouteInfoCollectorService가 호출되지 않는 상황이기 때문에
        // RouteInfoCollectorService를 통해 초기화 되는 UrlStateNotifactionService 의 URL 초기화 정보를 사용하면 안됨.
        this.activatedRoute.data.subscribe((urlData: any) => {
            const params = this.getUrlParams();
            if (params.period) {
                this.urlRouteManagerService.move({
                    url: [
                        urlData['path'] || params.startPath,
                        params.application,
                        params.period
                    ],
                    needServerTimeRequest: true
                });
            } else {
                this.urlRouteManagerService.move({
                    url: [
                        urlData['path'] || params.startPath,
                        params.application,
                        this.webAppSettingDataService.getUserDefaultPeriod().getValueWithTime()
                    ],
                    needServerTimeRequest: true
                });
            }
        });
    }
    private getUrlParams(): any {
        const params: { [key: string]: string } = {};
        let activatedRoute: ActivatedRoute | null = this.activatedRoute;
        while ( activatedRoute ) {
            activatedRoute.snapshot.paramMap.keys.forEach((key: string) => {
                params[key] = activatedRoute.snapshot.paramMap.get(key);
            });
            if ( activatedRoute.parent === null ) {
                break;
            } else {
                activatedRoute = activatedRoute.parent;
            }
        }
        params['startPath'] = activatedRoute.snapshot.firstChild.firstChild.firstChild.url[0].path;
        return params;
    }
}
