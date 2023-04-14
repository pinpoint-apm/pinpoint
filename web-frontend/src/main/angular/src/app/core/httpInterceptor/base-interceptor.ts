import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

const urlPrefix = '/';

@Injectable()
export class BaseInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let patchedReq = req;
        if (/.*\.pinpoint(ws)?$/.test(req.url)) {
            patchedReq = req.clone({
                url: urlPrefix + req.url
            });
        }

        return next.handle(patchedReq);
    }
}
