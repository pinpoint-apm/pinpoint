import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';

import { WebAppSettingDataService } from 'app/shared/services/web-app-setting-data.service';
import { UrlRouteManagerService } from 'app/shared/services/url-route-manager.service';

export const enum Theme {
    Dark = 'dark-mode',
    Light = 'light-mode',
}

@Injectable()
export class ThemeService {
    private renderer: Renderer2;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private urlRouteManagerService: UrlRouteManagerService,
        private rendererFactory: RendererFactory2,
    ) {
        this.renderer = this.rendererFactory.createRenderer(null, null);
    }

    changeTheme(theme: Theme[keyof Theme]): void {
        this.webAppSettingDataService.setTheme(theme as string);
        this.urlRouteManagerService.reload();
    }

    setTheme(): void {
        const theme = this.webAppSettingDataService.getTheme();

        this.renderer.addClass(document.body, theme);
    }

    getTheme(): Theme {
        return this.webAppSettingDataService.getTheme();
    }
}
