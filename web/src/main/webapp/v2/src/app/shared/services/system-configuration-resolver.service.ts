import { Injectable } from '@angular/core';
import { Router, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SystemConfigurationDataService } from './system-configuration-data.service';

@Injectable()
export class SystemConfigurationResolverService implements Resolve<ISystemConfiguration> {
    constructor(
        private router: Router,
        private systemConfigurationDataService: SystemConfigurationDataService
    ) {}
    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ISystemConfiguration> {
        return this.systemConfigurationDataService.getConfiguration().pipe(
            catchError((error: any) => {
                this.router.navigate(['/error']);
                return EMPTY;
            })
        );
    }
}
