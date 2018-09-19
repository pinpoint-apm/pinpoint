import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface IGroupMember {
    department: string;
    memberId: string;
    name: string;
    number: string;
    userGroupId: string;
}
export interface IGroupMemberResponse {
    result: string;
}

@Injectable()
export class GroupMemberDataService {
    url = 'userGroup/member.pinpoint';
    constructor(private http: HttpClient) { }
    retrieve(userGroupId: string): Observable<IGroupMember[]> {
        return this.http.get<IGroupMember[]>(this.url, { params: { userGroupId }}).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    create(memberId: string, userGroupId: string): Observable<IGroupMemberResponse> {
        return this.http.post<IGroupMemberResponse>(this.url, {
            memberId,
            userGroupId
        }).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    remove(memberId: string, userGroupId: string): Observable<IGroupMemberResponse> {
        return this.http.request<IGroupMemberResponse>('delete', this.url, {
            body: {
                memberId,
                userGroupId
            }
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
}
