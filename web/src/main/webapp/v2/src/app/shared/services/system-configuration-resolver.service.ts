import { Injectable } from '@angular/core';
import { Router, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { SystemConfigurationDataService } from './system-configuration-data.service';
import { UrlPath } from 'app/shared/models';

@Injectable()
export class SystemConfigurationResolverService implements Resolve<ISystemConfiguration> {
    constructor(
        private router: Router,
        private systemConfigurationDataService: SystemConfigurationDataService
    ) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ISystemConfiguration> | Observable<never> {
        return this.systemConfigurationDataService.getConfiguration().pipe(
            catchError(() => {
                this.router.navigate([`/${UrlPath.ERROR}`]);
                return EMPTY;
            })
        );
    }
}
