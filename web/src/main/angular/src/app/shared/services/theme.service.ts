import { Injectable } from '@angular/core';

import * as themeMap from 'app/core/constants/theme';

export const enum Theme {
    Light = 'light',
}

@Injectable()
export class ThemeService {
    constructor() {}
    private setTheme(theme: Theme): void {
        Object.entries(themeMap[theme]).forEach(([key, value]) => {
            document.documentElement.style.setProperty(`--${key}`, value);
        });
    }

    changeTheme(theme: Theme): void {
        this.setTheme(theme);
    }
}
