import { Injectable } from '@angular/core';

@Injectable()
export class AjaxExceptionCheckerService {
    constructor() {
    }

    isAjaxException(data: AjaxException | any): data is AjaxException {
        return (<AjaxException>data).exception !== undefined;
    }
}
