import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { WebAppSettingDataService } from 'app/shared/services';

@Injectable({
    providedIn: 'root'
})
export class FavoriteApplicationListDataService {
    // private url = 'userConfiguration/favoriteApplications.pinpoint';

    constructor(
        private http: HttpClient,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    getFavoriteApplicationList(): Observable<IApplication[]> {
        return of(this.webAppSettingDataService.getFavoriteApplicationList());
        // return this.http.get<{favoriteApplications: IApplication[]}>(this.url);
    }

    // saveFavoriteList(favoriteApplications: IApplication[]): Observable<any> {
        // return this.http.put<{favoriteApplications: IApplication[]}>(this.url, {
        //     favoriteApplications
        // });
    // }
}
