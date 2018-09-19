import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface IApplicationAvailable {
    code: number;
    message: string;
}

@Injectable()
export class ApplicationNameDuplicationCheckDataService {
    private requestURL = 'isAvailableApplicationName.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}
    getResponseWithParams(value: string): Observable<IApplicationAvailable> {
        return this.http.get<IApplicationAvailable>(this.requestURL, this.makeParams({applicationName: value})).pipe(
            catchError(this.handleError)
        );
    }
    private makeParams(paramObj: object): object {
        return {
            params: { ...paramObj }
        };
    }
    private handleError(error: HttpErrorResponse | string) {
        return throwError(error['statusText'] || error);
    }
}
