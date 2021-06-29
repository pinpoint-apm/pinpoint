import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

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
    private url = 'userGroup/member.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    retrieve(userGroupId: string): Observable<IGroupMember[]> {
        return this.http.get<IGroupMember[]>(this.url, { params: new HttpParams().set('userGroupId', userGroupId) });
    }

    create(memberId: string, userGroupId: string): Observable<IGroupMemberResponse> {
        return this.http.post<IGroupMemberResponse>(this.url, {
            memberId,
            userGroupId
        });
    }

    remove(memberId: string, userGroupId: string): Observable<IGroupMemberResponse> {
        return this.http.request<IGroupMemberResponse>('delete', this.url, {
            body: {
                memberId,
                userGroupId
            }
        });
    }
}
