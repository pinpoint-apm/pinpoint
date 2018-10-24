import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IApplicationAvailable {
    code: number;
    message: string;
}

@Injectable()
export class ApplicationNameDuplicationCheckDataService {
    private requestURL = 'isAvailableApplicationName.pinpoint';
    constructor(private http: HttpClient) {}
    getResponseWithParams(value: string): Observable<IApplicationAvailable> {
        return this.http.get<IApplicationAvailable>(this.requestURL, {
            params: new HttpParams().set('applicationName', value)
        });
    }
}
