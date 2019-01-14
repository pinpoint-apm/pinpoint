import { Injectable } from '@angular/core';
import { Router, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';

@Injectable()
export class ApplicationListResolverService implements Resolve<IApplication[]> {
    constructor(
        private router: Router,
        private applicationListDataService: ApplicationListDataService) { }
    resolve(reoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IApplication[]> {
        return this.applicationListDataService.getApplicationList().pipe(
            catchError((error: any) => {
                this.router.navigate(['/error']);
                return EMPTY;
            })
        );
    }
}
