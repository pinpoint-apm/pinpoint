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
            catchError((error: HttpErrorResponse) => {
                if (error.error instanceof ErrorEvent) {
                    // * client-side error
                    console.error('client-side error has occurred', error.error);
                    // TODO: Test client-side error
                    return throwError({
                        message: error.error.message,
                        data: {
                            requestInfo: {
                                url: error.error.filename
                            }
                        }
                    });
                    // return throwError({
                    //     exception: {
                    //         request: {
                    //             url: error.error.filename
                    //         },
                    //         message: error.error.message
                    //     }
                    // });
                } else {
                    if (error.status === 401) {
                        this.authService.onAuthError();
                        return EMPTY;
                    } else {
                        // TODO: Test server-side error
                        // * server-side error
                        console.error('server-side error has occurred', error);
                        // return throwError(error);
                        return throwError(error.error);
                    }
                }
            })
        );
    }
}
