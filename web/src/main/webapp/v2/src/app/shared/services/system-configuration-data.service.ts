import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class SystemConfigurationDataService {
    private url = 'configuration.pinpoint';
    private defaultConfiguration: ISystemConfiguration = {
        editUserInfo: false,
        enableServerMapRealTime: false,
        openSource: true,
        sendUsage: true,
        showActiveThread: false,
        showActiveThreadDump: false,
        showApplicationStat: false,
        version: '',
        userId: '',
        userName: '',
        userDepartment: ''
    };

    constructor(
        private http: HttpClient
    ) {}

    getConfiguration(): Observable<ISystemConfiguration> {
        return this.http.get<ISystemConfiguration>(this.url).pipe(
            map(res => {
                if (res) {
                    return res;
                } else {
                    return this.defaultConfiguration;
                }
            })
        );
    }
}
