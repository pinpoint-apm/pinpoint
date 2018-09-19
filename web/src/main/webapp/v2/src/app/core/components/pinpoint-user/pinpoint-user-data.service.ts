import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface IPinpointUser {
    department?: string;
    email: string;
    name: string;
    number: string;
    phoneNumber: string;
    userId: string;
}
export interface IPinpointUserResponse {
    result: string;
}

@Injectable()
export class PinpointUserDataService {
    url = 'user.pinpoint';
    constructor(private http: HttpClient) { }
    retrieve(department?: string): Observable<IPinpointUser[] | {}> {
        return this.http.get<IPinpointUser[] | {}>(this.url, this.makeRequestOptionsArgs(department)).pipe(
            tap((data) => {
                if (data['errorCode']) {
                    throw data['errorMessage'];
                }
            }),
            catchError(this.handleError)
        );
    }
    create(params: IPinpointUser): Observable<IPinpointUserResponse> {
        return this.http.post<IPinpointUserResponse>(this.url, params).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    update(params: IPinpointUser): Observable<IPinpointUserResponse> {
        return this.http.put<IPinpointUserResponse>(this.url, params).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    remove(userId: string): Observable<IPinpointUserResponse> {
        return this.http.request<IPinpointUserResponse>('delete', this.url, {
            body: { userId }
        }).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    private checkError(data: any) {
        if (data['errorCode']) {
            throw data['errorMessage'];
        } else if (data['result'] !== 'SUCCESS') {
            throw data;
        }
    }
    private handleError(error: HttpErrorResponse | string) {
        return throwError(error['statusText'] || error);
    }
    private makeRequestOptionsArgs(department?: string): object {
        return {
            params: department ? { searchKey: department } : {}
        };
    }
}
