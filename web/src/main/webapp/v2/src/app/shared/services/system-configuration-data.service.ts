import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
export interface ISystemConfiguration {
    editUserInfo: boolean;
    enableServerMapRealTime: boolean;
    openSource: boolean;
    sendUsage: boolean;
    showActiveThread: boolean;
    showActiveThreadDump: boolean;
    showApplicationStat: boolean;
    version: string;
    userId?: string;
    userName?: string;
    userDepartment?: string;
}

@Injectable()
export class SystemConfigurationDataService {
    url = 'configuration.pinpoint';
    constructor(private http: HttpClient) {}
    getConfiguration(): Observable<ISystemConfiguration> {
        return this.http.get<ISystemConfiguration>(this.url).pipe(
            map(res => {
                if (res) {
                    return res;
                } else {
                    return {
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
                }
            })
        );
    }
}
