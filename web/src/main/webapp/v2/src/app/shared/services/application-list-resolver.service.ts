import { Injectable } from '@angular/core';
import { Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable } from 'rxjs';

import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';

@Injectable()
export class ApplicationListResolverService implements Resolve<IApplication[]> {
    constructor(
        private applicationListDataService: ApplicationListDataService) { }
    resolve(reoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IApplication[]> {
        return this.applicationListDataService.getApplicationList();
    }
}
