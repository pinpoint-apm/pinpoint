import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

const validUrlList: string[] = [];
const urlPrefix = '/';

@Injectable()
export class MarkingInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let patchedReq = req;
        if (/.*\.pinpoint(ws)?$/.test(req.url)) {
            patchedReq = req.clone({
                url: urlPrefix + req.url
            });
        }
        return next.handle(patchedReq).pipe(
            map((event => {
                if (event instanceof HttpResponse) {
                    if (!(event.body instanceof Array)) {
                        if (this.isValidUrl(event.url)) {
                            event = event.clone({
                                body: { ...event.body, 'pinpointPageBaseApi': true }
                            });
                        }
                    }
                }
                return event;
            })),
            catchError((error: HttpErrorResponse) => {
                if (error.status < 500) {
                    return throwError({
                        exception: {
                            request: {
                                url: error.url
                            },
                            message: error.message
                        }
                    });
                } else {
                    return throwError(error.error);
                }
            })
        );
    }
    private isValidUrl(url: string): boolean {
        const index = validUrlList.findIndex((validUrl: string) => {
            return validUrl.indexOf(url) !== -1;
        });
        return index >= 0 ? true : false;
    }
}
