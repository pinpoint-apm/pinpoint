import { Injectable } from '@angular/core';
import { Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ServerTimeDataService } from 'app/shared/services/server-time-data.service';

@Injectable()
export class ServerTimeResolverService implements Resolve<number> {
    constructor(
        private serverTimeService: ServerTimeDataService
    ) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<number> {
        return this.serverTimeService.getServerTime().pipe(
            catchError((error: IServerErrorFormat) => {
                return of(Date.now());
            })
        );
    }
}
