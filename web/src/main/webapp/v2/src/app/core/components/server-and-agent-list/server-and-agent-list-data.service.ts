import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services';

@Injectable()
export class ServerAndAgentListDataService {
    private url = 'getAgentList.pinpoint';
    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) { }
    getData(): Observable<any> {
        return this.http.get<{[key: string]: IServerAndAgentData[]}>(this.url, this.makeRequestOptionsArgs()).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(): any {
        return {
            params: new HttpParams()
                .set('application', this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).applicationName)
                .set('from', this.newUrlStateNotificationService.getStartTimeToNumber() + '')
                .set('to', this.newUrlStateNotificationService.getEndTimeToNumber() + '')
        };
    }
}
