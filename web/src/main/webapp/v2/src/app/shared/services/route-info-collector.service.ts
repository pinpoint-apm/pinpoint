import { Injectable } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, NavigationEnd, ParamMap } from '@angular/router';
import { filter } from 'rxjs/operators';
import { NewUrlStateNotificationService } from './new-url-state-notification.service';
import { AnalyticsService } from 'app/shared/services/analytics.service';

@Injectable()
export class RouteInfoCollectorService {
    constructor(
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private analyticsService: AnalyticsService
    ) {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe((event: NavigationEnd) => {
            const startPath = this.activatedRoute.snapshot.root.firstChild.url[0].path;
            const innerData = {};
            const pathIds = {};
            const queryParams = {};
            this.collectUrlInfo(this.activatedRoute.snapshot.children, pathIds, queryParams, innerData);
            this.newUrlStateNotificationService.updateUrl(startPath, pathIds, queryParams, innerData, this.activatedRoute.children[0].children[0]);
            this.analyticsService.trackPage(startPath);
        });
    }
    private collectUrlInfo(activatedChildRouteSnapshot: ActivatedRouteSnapshot[], pathIds: any, queryParams: any, innerData: any): void {
        if (activatedChildRouteSnapshot.length !== 0) {
            for (let i = 0; i < activatedChildRouteSnapshot.length; i++) {
                this.assign(pathIds, activatedChildRouteSnapshot[i].paramMap);
                this.assign(queryParams, activatedChildRouteSnapshot[i].queryParamMap);
                Object.assign(innerData, activatedChildRouteSnapshot[i].data);
                this.collectUrlInfo(activatedChildRouteSnapshot[i].children, pathIds, queryParams, innerData);
            }
        }
    }
    private assign(data: any, mapData: ParamMap): void {
        mapData.keys.forEach((key: string) => {
            data[key] = mapData.get(key);
        });
    }
}
