import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { MarkingInterceptor } from './marking-interceptor';

export const httpInterceptorProviders = [
    { provide: HTTP_INTERCEPTORS, useClass: MarkingInterceptor, multi: true }
];
