import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface IUserGroup {
    id: string;
    number: string;
}
export interface IUserGroupCreated {
    number: string;
}
export interface IUserGroupDeleted {
    result: string;
}

@Injectable()
export class UserGroupDataService {
    url = 'userGroup.pinpoint';
    constructor(private http: HttpClient) { }
    retrieve(param?: any): Observable<IUserGroup[]> {
        return this.http.get<IUserGroup[]>(this.url, this.makeRequestOptionsArgs(param)).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    create(id: string, userId: string): Observable<IUserGroupCreated> {
        return this.http.post<IUserGroupCreated>(this.url, this.makeCreateRemoveOptionsArgs(id, userId)).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    remove(id: string, userId: string): Observable<IUserGroupDeleted> {
        return this.http.request<IUserGroupDeleted>('delete', this.url, {
            body: this.makeCreateRemoveOptionsArgs(id, userId)
        }).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText || error);
    }
    private makeRequestOptionsArgs(param?: any): object {
        return param ? {
            params: param
        } : {};
    }
    private makeCreateRemoveOptionsArgs(id: string, userId: string): object {
        return {
            id: id,
            userId: userId
        };
    }
}
