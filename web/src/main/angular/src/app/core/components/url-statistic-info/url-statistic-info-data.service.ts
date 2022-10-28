import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class UrlStatisticInfoDataService {
    private url = 'uriStat/top50.pinpoint';

    constructor(
        private http: HttpClient,
    ) { }
    
}

// TODO: url-info부터 작업하면 됨