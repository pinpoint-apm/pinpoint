import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpResponse } from '@angular/common/http';

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

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
            }))
        );
    }
    private isValidUrl(url: string): boolean {
        const index = validUrlList.findIndex((validUrl: string) => {
            return validUrl.indexOf(url) !== -1;
        });
        return index >= 0 ? true : false;
    }
}
