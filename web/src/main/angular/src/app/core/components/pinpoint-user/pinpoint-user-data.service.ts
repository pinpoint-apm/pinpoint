import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IPinpointUserResponse {
    result: string;
}

@Injectable()
export class PinpointUserDataService {
    private url = 'user.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    retrieve(query?: string): Observable<IUserProfile[] | {}> {
        return this.http.get<IUserProfile[] | {}>(this.url, this.makeRequestOptionsArgs(query));
    }

    create(params: IUserProfile): Observable<IPinpointUserResponse> {
        return this.http.post<IPinpointUserResponse>(this.url, params);
    }

    update(params: IUserProfile): Observable<IPinpointUserResponse> {
        return this.http.put<IPinpointUserResponse>(this.url, params);
    }

    remove(userId: string): Observable<IPinpointUserResponse> {
        return this.http.request<IPinpointUserResponse>('delete', this.url, {
            body: { userId }
        });
    }

    private makeRequestOptionsArgs(query?: string): object {
        return query
            ? {params: {searchKey: query}}
            : {};
    }
}
