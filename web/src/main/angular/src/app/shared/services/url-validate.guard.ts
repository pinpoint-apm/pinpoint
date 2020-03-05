import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, ParamMap } from '@angular/router';

import { UrlRouteManagerService } from 'app/shared/services/url-route-manager.service';
import { WebAppSettingDataService } from 'app/shared/services/web-app-setting-data.service';
import { UrlPath, UrlPathId } from 'app/shared/models';

interface IPathParam {
    [key: string]: string;
}

@Injectable()
export class UrlValidateGuard implements CanActivate {
    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private urlRouteManagerService: UrlRouteManagerService
    ) {}
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        const hasPaths: IPathParam = {};
        this.collectUrlInfo(route.children, hasPaths);
        switch (route.routeConfig.path) {
            case UrlPath.MAIN:
                return this.checkMainRoute(route, hasPaths);
            case UrlPath.FILTERED_MAP:
                return this.checkFilteredMapRoute(route, hasPaths);
        }
        return false;
    }
    private checkMainRoute(route: ActivatedRouteSnapshot, hasPaths: IPathParam): boolean {
        const subPath = route.children[0].routeConfig.path;
        switch (subPath) {
            case ':' + UrlPathId.APPLICATION:
                this.urlRouteManagerService.move({
                    url: [
                        UrlPath.MAIN,
                        hasPaths.application,
                        this.webAppSettingDataService.getUserDefaultPeriod().getValueWithTime()
                    ],
                    needServerTimeRequest: true
                });
                return false;
            case ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD:
                this.urlRouteManagerService.move({
                    url: [
                        UrlPath.MAIN,
                        hasPaths.application,
                        hasPaths.period
                    ],
                    needServerTimeRequest: true
                });
                return false;
            case ':' + UrlPathId.APPLICATION + '/' + UrlPath.REAL_TIME:
            case ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME:
            default:
                return true;
        }
    }
    private checkFilteredMapRoute(route: ActivatedRouteSnapshot, hasPaths: IPathParam): boolean {
        const subPath = route.children[0].routeConfig.path;
        switch (subPath) {
            case ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME:
                return true;
            case ':' + UrlPathId.APPLICATION:
            case ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD:
            default:
                this.urlRouteManagerService.move({
                    url: [
                        UrlPath.MAIN
                    ],
                    needServerTimeRequest: false
                });
                return false;
        }
    }
    private collectUrlInfo(activatedChildRouteSnapshot: ActivatedRouteSnapshot[], hasPaths: any): void {
        if (activatedChildRouteSnapshot.length !== 0) {
            for ( let i = 0 ; i < activatedChildRouteSnapshot.length ; i++ ) {
                this.assign(hasPaths, activatedChildRouteSnapshot[i].paramMap);
                this.collectUrlInfo(activatedChildRouteSnapshot[i].children, hasPaths);
            }
        }
    }
    private assign(data: any, mapData: ParamMap): void {
        mapData.keys.forEach((key: string) => {
            data[key] = mapData.get(key);
        });
    }
}
