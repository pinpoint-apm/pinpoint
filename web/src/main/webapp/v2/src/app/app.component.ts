import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { WindowRefService } from 'app/shared/services';

@Component({
    selector: 'pp-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    constructor(
        private windowRefService: WindowRefService,
        private translateService: TranslateService
    ) {
        const supportLanguages = ['en', 'ko'];
        const defaultLang = 'en';
        const currentLang = this.windowRefService.nativeWindow.navigator.language.substring(0, 2);
        this.translateService.addLangs(supportLanguages);
        this.translateService.setDefaultLang(defaultLang);
        if (supportLanguages.find((lang: string) => {
            return lang === currentLang;
        })) {
            this.translateService.use(currentLang);
        } else {
            this.translateService.use(defaultLang);
        }
    }
    ngOnInit() {}
}
