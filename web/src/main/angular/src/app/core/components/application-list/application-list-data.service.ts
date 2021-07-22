import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { Application } from 'app/core/models/application';

@Injectable({
    providedIn: 'root'
})
export class ApplicationListDataService {
    private url = 'applications.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getApplicationList(): Observable<IApplication[]> {
        return this.http.get<IApplication[]>(this.url).pipe(
            // TODO: 워커적용?
            map((res: IApplication[]) => res.map(({applicationName, serviceType, code}) => new Application(applicationName, serviceType, code))),
        );
    }
}
