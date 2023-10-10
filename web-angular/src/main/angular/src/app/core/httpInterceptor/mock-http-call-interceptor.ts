import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpEvent, HttpHandler, HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

// import data from './data.json';

const exceptionObjMock = {
    exception: {
        message: 'java.lang.RuntimeException: org.apache.hadoop.hbase.client...',
        request: {
            method: 'GET',
            url: 'http://localhost:4200/xx/yy.pinpoint'
        },
        stacktrace: 'com.navercorp.pinpoint.common.hbase.parallel...'
    }
};
@Injectable()
export class MockHttpCallInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return req.url.includes('.pinpoint') ? this.handleRoutes(req, next) : next.handle(req);
    }

    handleRoutes(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const {url, method, body} = req;
        const apiName = url.match(/.*\/(.*)\.pinpoint/)[1];

        switch (apiName) {
            // case 'drag':
            // case 'transactionmetadata':
            // case 'getResponseTimeHistogramDataV2':
            // case 'transactionInfo':
                // console.log('여기');
                // return of(new HttpResponse({status: 200, body: exceptionObjMock})).pipe(delay(500));
                // return of(new HttpResponse({status: 200, body: data})).pipe(delay(500));
                // const error = new HttpErrorResponse({status: 501, url: 'https://test.com'});
                // error.message = 'Error Occurred';
                // return throwError();
                // return next.handle(req);
            // case 'getScatterData':
            //     // const error = new HttpErrorResponse({status: 501, url: 'https://test.com'});
            //     const error = new HttpErrorResponse({error: {
            //         trace: 'com.navercorp.pinpoint.web.PinpointError',
            //         message: 'message test',
            //         data: {
            //             requestInfo: {
            //                 method: 'POST',
            //                 url: '/some.pinpoint',
            //                 headers: {},
            //                 parameters: {}
            //             }
            //         }
            //     }});

            //     return throwError(error);
                // return next.handle(req);
            default:
                return next.handle(req);
        }
    }
}
