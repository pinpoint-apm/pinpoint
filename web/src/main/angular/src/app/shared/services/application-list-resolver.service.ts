import { Injectable } from '@angular/core';
import { Router, Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { ApplicationListDataService } from 'app/shared/services/application-list-data.service';
import { UrlPathId, UrlPath } from 'app/shared/models';

@Injectable()
export class ApplicationListResolverService implements Resolve<IApplication[]> {
    constructor(
        private router: Router,
        private applicationListDataService: ApplicationListDataService
    ) {}

    resolve(route: ActivatedRouteSnapshot): Observable<IApplication[]> | Observable<never> {
        return this.applicationListDataService.getApplicationList().pipe(
            tap((appList: IApplication[]) => {
                const providedApp = this.getAppKeyStr(route);
                const isAppIncludedFunc = (appKeyStr: string) => !!appList.find((app: IApplication) => app.getUrlStr() === appKeyStr);

                if (providedApp && !isAppIncludedFunc(providedApp)) {
                    this.router.navigate([`/${UrlPath.MAIN}`]);
                }
            }),
            catchError(() => {
                this.router.navigate([`/${UrlPath.ERROR}`]);
                return EMPTY;
            })
        );
    }

    private getAppKeyStr(route: ActivatedRouteSnapshot): string {
        let routeChild = route;
        let appKeyStr = '';

        while (routeChild) {
            const paramMap = routeChild.paramMap;

            if (paramMap.has(UrlPathId.APPLICATION)) {
                appKeyStr = paramMap.get(UrlPathId.APPLICATION);
                break;
            }

            routeChild = routeChild.firstChild;
        }

        return appKeyStr;
    }
}
