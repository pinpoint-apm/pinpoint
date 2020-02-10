import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services/store-helper.service';
import { Actions } from 'app/shared/store';
import { Application } from 'app/core/models/application';

@Injectable()
export class ApplicationListDataService {
    private url = 'applications.pinpoint';

    constructor(
        private http: HttpClient,
        private storeHelperService: StoreHelperService
    ) {}

    getApplicationList(): Observable<IApplication[]> {
        return this.http.get<IApplication[]>(this.url).pipe(
            map((res = [] as IApplication[]) => res.map(app => new Application(app.applicationName, app.serviceType, app.code))),
            tap((appList: IApplication[]) => this.storeHelperService.dispatch(new Actions.UpdateApplicationList(appList))),
            map((appList: IApplication[]) => appList)
        );
    }
}
