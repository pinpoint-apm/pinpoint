import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-language-setting',
    templateUrl: './language-setting.component.html',
    styleUrls: ['./language-setting.component.css']
})
export class LanguageSettingComponent implements OnInit {
    @Input() currentLanguage: string;
    @Input() languagelist: {[key: string]: string}[];
    @Output() outChangeLanguage = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    onChangeLanguage(lang: string): void {
        this.outChangeLanguage.emit(lang);
    }
}
