import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

import { UrlQuery, UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, WebAppSettingDataService } from 'app/shared/services';

@Injectable()
export class ServerMapDataService {
    private url = 'getServerMapDataV2.pinpoint';
    constructor(
        private http: HttpClient,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}

    getData([from, to]: number[]): Observable<IServerMapInfo> {
        return this.http.get<IServerMapInfo>(this.url, this.makeRequestOptionsArgs(from, to)).pipe(
            retry(3)
        );
    }

    private makeRequestOptionsArgs(from: number, to: number): object {
        return {
            params: new HttpParams()
                .set('applicationName', this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName())
                .set('serviceTypeName', this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getServiceType())
                .set('from', from + '')
                .set('to', to + '')
                .set('calleeRange', this.newUrlStateNotificationService.hasValue(UrlQuery.INBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.INBOUND) : this.webAppSettingDataService.getSystemDefaultInbound())
                .set('callerRange', this.newUrlStateNotificationService.hasValue(UrlQuery.OUTBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.OUTBOUND) : this.webAppSettingDataService.getSystemDefaultOutbound())
                .set('wasOnly', this.newUrlStateNotificationService.hasValue(UrlQuery.WAS_ONLY) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.WAS_ONLY) : false)
                .set('bidirectional', this.newUrlStateNotificationService.hasValue(UrlQuery.BIDIRECTIONAL) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.BIDIRECTIONAL) : false)
        };
    }
}
