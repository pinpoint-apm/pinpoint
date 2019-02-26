import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

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
    private url = 'user.pinpoint';
    constructor(private http: HttpClient) { }
    retrieve(department?: string): Observable<IPinpointUser[] | {}> {
        department = department || 'PaaS';
        return this.http.get<IPinpointUser[] | {}>(this.url, this.makeRequestOptionsArgs(department)).pipe(
            retry(3)
        );
    }
    create(params: IPinpointUser): Observable<IPinpointUserResponse> {
        return this.http.post<IPinpointUserResponse>(this.url, params).pipe(
            retry(3)
        );
    }
    update(params: IPinpointUser): Observable<IPinpointUserResponse> {
        return this.http.put<IPinpointUserResponse>(this.url, params).pipe(
            retry(3)
        );
    }
    remove(userId: string): Observable<IPinpointUserResponse> {
        return this.http.request<IPinpointUserResponse>('delete', this.url, {
            body: { userId }
        }).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(department?: string): object {
        return department ? {
            params: new HttpParams().set('searchKey', department)
        } : {};
    }
}
