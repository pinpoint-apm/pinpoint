import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
        return this.http.get<IServerMapInfo>(this.url, this.makeRequestOptionsArgs(from, to));
    }

    private makeRequestOptionsArgs(from: number, to: number): object {
        return {
            params: {
                applicationName: this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName(),
                serviceTypeName: this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getServiceType(),
                from,
                to,
                calleeRange: this.newUrlStateNotificationService.hasValue(UrlQuery.INBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.INBOUND) : this.webAppSettingDataService.getSystemDefaultInbound(),
                callerRange: this.newUrlStateNotificationService.hasValue(UrlQuery.OUTBOUND) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.OUTBOUND) : this.webAppSettingDataService.getSystemDefaultOutbound(),
                wasOnly: this.newUrlStateNotificationService.hasValue(UrlQuery.WAS_ONLY) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.WAS_ONLY) : false,
                bidirectional: this.newUrlStateNotificationService.hasValue(UrlQuery.BIDIRECTIONAL) ? this.newUrlStateNotificationService.getQueryValue(UrlQuery.BIDIRECTIONAL) : false
            }
        };
    }
}
