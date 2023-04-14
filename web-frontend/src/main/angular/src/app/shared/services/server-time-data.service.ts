import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

interface IServerTime {
    currentServerTime: number;
}

@Injectable()
export class ServerTimeDataService {
    constructor(
        private http: HttpClient
    ) {}

    getServerTime(): Observable<number> {
        return this.http.get<IServerTime>('serverTime.pinpoint').pipe(
            map(res => {
                return res.currentServerTime;
            })
        );
    }
}
