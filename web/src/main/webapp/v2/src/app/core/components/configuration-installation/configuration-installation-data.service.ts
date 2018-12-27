import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

export interface IInstallationData {
    code: number;
    message: {
        downloadUrl: string,
        installationArgument: string,
        version: string
    };
}

@Injectable()
export class ConfigurationInstallationDataService {
    private dataRequestURL = 'getAgentInstallationInfo.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getData(): Observable<IInstallationData> {
        return this.http.get<IInstallationData>(this.dataRequestURL).pipe(
            retry(3)
        );
    }
}
