import { Injectable } from '@angular/core';
import { Router, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ApplicationListDataService } from 'app/shared/services/application-list-data.service';

@Injectable()
export class ApplicationListResolverService implements Resolve<IApplication[]> {
    constructor(
        private router: Router,
        private applicationListDataService: ApplicationListDataService
    ) {}

    // TODO: #2342 Guard the application param => empty main으로 보내기.
    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IApplication[]> {
        return this.applicationListDataService.getApplicationList().pipe(
            catchError((error: any) => {
                this.router.navigate(['/error']);
                return EMPTY;
            })
        );
    }
}
