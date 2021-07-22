import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, EMPTY, of } from 'rxjs';
import { catchError, retry, switchMap } from 'rxjs/operators';

import { AuthService } from 'app/shared/services';
import { isThatType } from 'app/core/utils/util';


@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    constructor(
        private authService: AuthService
    ) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> | Observable<never> {
        return next.handle(req).pipe(
            retry(3),
            switchMap((event: HttpEvent<any>) => {
                if (event instanceof HttpResponse) {
                    if (isThatType<IServerErrorFormat>(event.body, 'exception')) {
                        return throwError(new HttpErrorResponse({error: event.body}));
                    } else {
                        return of(event);
                    }
                }
                return of(event);
            }),
            catchError((error: HttpErrorResponse) => {
                if (error.error instanceof ErrorEvent) {
                    // * client-side error
                    console.error('client-side error has occurred', error.error);
                    return throwError({
                        exception: {
                            request: {
                                url: error.error.filename
                            },
                            message: error.error.message
                        }
                    });
                } else {
                    if (isThatType<IServerErrorFormat>(error.error, 'exception')) {
                        return throwError(error.error);
                    } else if (error.status === 401) {
                        this.authService.onAuthError();
                        return EMPTY;
                    } else {
                        // * server-side error
                        console.error('server-side error has occurred', error);
                        return throwError({
                            exception: {
                                request: {
                                    url: error.url
                                },
                                message: error.message
                            }
                        });
                    }
                }
            })
        );
    }
}
