import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Observable, throwError, EMPTY } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { AuthService } from 'app/shared/services';

const validUrlList: string[] = [];
const urlPrefix = '/';

@Injectable()
export class MarkingInterceptor implements HttpInterceptor {
    constructor(
        private authService: AuthService
    ) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> | Observable<never> {
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
                if (error.status === 401) {
                    this.authService.onAuthError();
                    return EMPTY;
                } else if (error.status < 500) {
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
