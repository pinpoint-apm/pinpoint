import { Injectable } from '@angular/core';
import { ParamMap, ActivatedRoute } from '@angular/router';

import { AnalyticsService } from './analytics.service';
import { NewUrlStateNotificationService } from './new-url-state-notification.service';
import { UrlRouteManagerService } from './url-route-manager.service';

@Injectable()
export class RouteInfoCollectorService {
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService
    ) {}

    collectUrlInfo(activatedRoute: ActivatedRoute): void {
        /**
         * snapshot: is the constructed route itself.
         * firstChild: starts from the path definition, e.g. the very root path with the resolve at the moment.
         */
        const secondDepthRoute = activatedRoute.snapshot.firstChild.firstChild; // the path right below the root empty string path
        const startPath = secondDepthRoute.url.length !== 0 ? secondDepthRoute.url[0].path : secondDepthRoute.firstChild.url[0].path;
        const pathIdMap = new Map<string, string>();
        const queryParamMap = new Map<string, string>();
        let innerData = {};
        let routeChild = activatedRoute.snapshot.firstChild;

        while (routeChild) {
            this.setData(pathIdMap, routeChild.paramMap);
            this.setData(queryParamMap, routeChild.queryParamMap);
            innerData = { ...innerData, ...routeChild.data };
            routeChild = routeChild.firstChild;
        }

        this.newUrlStateNotificationService.updateUrl(startPath, pathIdMap, queryParamMap, innerData, activatedRoute.firstChild.firstChild);
        this.analyticsService.trackPage(startPath + this.urlRouteManagerService.getBaseHref().replace(/(.*)(\/)$/, '$1'));
    }

    private setData(dataMap: Map<string, string>, routeData: ParamMap): void {
        routeData.keys.forEach((key: string) => {
            dataMap.set(key, routeData.get(key));
        });
    }
}
