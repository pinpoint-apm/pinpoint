import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

enum TYPE {
    SQL = 'SQL',
    JSON = 'JSON'
}

@Injectable()
export class SyntaxHighlightDataService {
    private sqlRequestURL = 'sqlBind.pinpoint';
    private jsonRequestURL = 'jsonBind.pinpoint';
    constructor(
        private http: HttpClient
    ) { }
    getData({type, originalContents, bindValue}: ISyntaxHighlightData): Observable<string> {
        let requestURL;
        switch (type) {
            case TYPE.SQL:
                requestURL = this.sqlRequestURL;
                break;
            case TYPE.JSON:
                requestURL = this.jsonRequestURL;
                break;
        }
        return this.http.post<string>(
            requestURL,
            `${type.toLowerCase()}=${encodeURIComponent(originalContents)}&bind=${encodeURIComponent(bindValue)}`,
            {
                headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
            }
        ).pipe(
            retry(3)
        );
    }
}
