import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services';

@Injectable()
export class ServerAndAgentListDataService {
    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) { }
    getData(): Observable<{ [key: string]: IServerAndAgentData[] }> {
        return this.http.get<{ [key: string]: IServerAndAgentData[] }>('getAgentList.pinpoint', this.makeRequestOptionsArgs());
    }
    private makeRequestOptionsArgs(): object {
        return {
            params: {
                application: this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).applicationName,
                from: this.newUrlStateNotificationService.getStartTimeToNumber(),
                to: this.newUrlStateNotificationService.getEndTimeToNumber()
            }
        };
    }
}
