import { Injectable } from '@angular/core';

function getWindow(): any {
    return window;
}

@Injectable()
export class WindowRefService {
    get nativeWindow(): any {
        return getWindow();
    }
}
