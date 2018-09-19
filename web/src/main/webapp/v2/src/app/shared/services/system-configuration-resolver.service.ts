import { Injectable } from '@angular/core';
import { Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { SystemConfigurationDataService, ISystemConfiguration } from './system-configuration-data.service';

@Injectable()
export class SystemConfigurationResolverService implements Resolve<ISystemConfiguration> {
    constructor(private systemConfigurationDataService: SystemConfigurationDataService) {}
    resolve(reoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ISystemConfiguration> {
        return this.systemConfigurationDataService.getConfiguration();
    }
}
