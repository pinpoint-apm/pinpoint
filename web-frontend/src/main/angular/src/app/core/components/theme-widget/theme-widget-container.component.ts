import { Component, OnInit, ElementRef } from '@angular/core';

import {
    AnalyticsService,
    TRACKED_EVENT_LIST,
    ThemeService,
    WebAppSettingDataService
} from 'app/shared/services';

@Component({
    selector: 'pp-theme-widget',
    templateUrl: './theme-widget-container.component.html',
    styleUrls: ['./theme-widget-container.component.css'],
})
export class ThemeWidgetContainerComponent implements OnInit {
    currentTheme: string;

    constructor(
        private el: ElementRef,
        private analyticsService: AnalyticsService,
        private webAppSettingDataService: WebAppSettingDataService,
        private themeService: ThemeService,
    ) {}

    ngOnInit() {
        this.currentTheme = this.webAppSettingDataService.getTheme();
    }

    onChangeTheme(theme: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_THEME, theme);
        this.themeService.changeTheme(theme);
    }

    onClickTheme($event: MouseEvent): void {
      const target = $event.target as HTMLElement;

      if (!Array.from(target.classList).includes('active')) {
          const theme = target.dataset.theme;

          this.onChangeTheme(theme);
      }
    }

    isThemeActive(themeButtonElement: HTMLButtonElement): boolean {
        return themeButtonElement.dataset.theme === this.currentTheme ? true : false;
    }
}
