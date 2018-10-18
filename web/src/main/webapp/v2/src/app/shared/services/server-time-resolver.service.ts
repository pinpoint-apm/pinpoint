import { Injectable } from '@angular/core';
import { Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { ServerTimeDataService } from 'app/shared/services/server-time-data.service';

@Injectable()
export class ServerTimeResolverService implements Resolve<number> {

    constructor(private serverTimeService: ServerTimeDataService) { }
    resolve(reoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<number> {
        return this.serverTimeService.getServerTimeToPromise().catch(() => {
            return Date.now();
        });
    }
}
