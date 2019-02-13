import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

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
    private url = 'userGroup.pinpoint';
    constructor(private http: HttpClient) { }
    retrieve(param?: any): Observable<IUserGroup[]> {
        return this.http.get<IUserGroup[]>(this.url, this.makeRequestOptionsArgs(param)).pipe(
            retry(3)
        );
    }
    create(id: string, userId: string): Observable<IUserGroupCreated> {
        return this.http.post<IUserGroupCreated>(this.url, this.makeCreateRemoveOptionsArgs(id, userId)).pipe(
            retry(3)
        );
    }
    remove(id: string, userId: string): Observable<IUserGroupDeleted> {
        return this.http.request<IUserGroupDeleted>('delete', this.url, {
            body: this.makeCreateRemoveOptionsArgs(id, userId)
        }).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(param?: any): object {
        if (param) {
            let httpParams = new HttpParams();
            Object.keys(param).forEach((key: string) => {
                httpParams = httpParams.set(key, param[key]);
            });
            return {
                params: httpParams
            };
        } else {
            return {};
        }
    }
    private makeCreateRemoveOptionsArgs(id: string, userId: string): object {
        return {
            id: id,
            userId: userId
        };
    }
}
