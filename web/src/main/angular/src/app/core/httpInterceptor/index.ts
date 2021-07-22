import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { BaseInterceptor } from './base-interceptor';
import { MockHttpCallInterceptor } from './mock-http-call-interceptor';
import { ErrorInterceptor } from './error-interceptor';

export const httpInterceptorProviders = [
    /**
     * * Consider the order of interceptors.
     * * Suppose, interceptors are registered as A, B, C in order:
     * * Request: A -> B -> C
     * * Response: C -> B -> A
     */
    { provide: HTTP_INTERCEPTORS, useClass: BaseInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    // { provide: HTTP_INTERCEPTORS, useClass: MockHttpCallInterceptor, multi: true },
];
