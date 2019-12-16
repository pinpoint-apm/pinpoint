import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

import { WindowRefService, RouteInfoCollectorService } from 'app/shared/services';

@Component({
    selector: 'pp-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    constructor(
        private windowRefService: WindowRefService,
        private translateService: TranslateService,
        private routeInfoCollectorService: RouteInfoCollectorService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.setLang();
        this.listenToRouter();
    }

    private setLang(): void {
        const supportLanguages = ['en', 'ko'];
        const defaultLang = 'en';
        const currentLang = this.windowRefService.nativeWindow.navigator.language.substring(0, 2);

        this.translateService.addLangs(supportLanguages);
        this.translateService.setDefaultLang(defaultLang);

        supportLanguages.find((lang: string) => lang === currentLang)
            ? this.translateService.use(currentLang)
            : this.translateService.use(defaultLang);
    }

    private listenToRouter(): void {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
            this.routeInfoCollectorService.collectUrlInfo(this.activatedRoute);
        });
    }
}
