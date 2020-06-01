import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

import { RouteInfoCollectorService, ThemeService, Theme, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    constructor(
        private translateService: TranslateService,
        private routeInfoCollectorService: RouteInfoCollectorService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private themeService: ThemeService,
        private storeHelperService: StoreHelperService,
    ) {}

    ngOnInit() {
        this.setLang();
        this.setTheme();
        this.listenToRouter();
    }

    private setLang(): void {
        this.storeHelperService.getLanguage().subscribe((lang: string) => {
            this.translateService.use(lang);
        });
    }

    private setTheme(): void {
        // Fetch the user's theme in the future
        // in the meantime, set the default theme at the moment
        this.themeService.changeTheme(Theme.Light);
    }

    private listenToRouter(): void {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
            this.routeInfoCollectorService.collectUrlInfo(this.activatedRoute);
        });
    }
}
