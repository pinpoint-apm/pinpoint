import { Injectable } from '@angular/core';

import { Theme, ThemeService } from 'app/shared/services';

@Injectable()
export class InspectorChartThemeService {
    constructor(
        private themeService: ThemeService,
    ) {}

    get isDarkMode() {
      return this.themeService.getTheme() === Theme.Dark;
    }

    getAlpha(defaultAlpha: number) {
        return this.isDarkMode ? 1.1 - defaultAlpha : defaultAlpha;
    }

    getMinAvgMaxColors() {
      return {
        min: '#05aff9',
        avg: '#5d19a3',
        max: '#0066CC',
      }
    }
}
