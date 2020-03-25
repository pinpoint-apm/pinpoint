declare var PR: any;
import { Component, OnInit, Input, Output, EventEmitter, HostBinding, AfterViewChecked } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { ClipboardService } from 'ngx-clipboard';
import { js_beautify } from 'js-beautify';
import sqlFormatter from 'sql-formatter';

@Component({
    selector: 'pp-syntax-highlight-popup',
    templateUrl: './syntax-highlight-popup.component.html',
    styleUrls: ['./syntax-highlight-popup.component.css'],
    animations: [
        trigger('showHide', [
            transition(':enter', [
                style({ opacity: 0 }),
                animate('1s ease-in-out', style({ opacity: 1 }))
            ]),
            transition(':leave', [
                animate('.5s ease-in-out', style({ opacity: 0 }))
            ])
        ]),
    ]
})
export class SyntaxHighlightPopupComponent implements OnInit, AfterViewChecked {
    @Input() data: ISyntaxHighlightData;
    @Output() outClosePopup = new EventEmitter<void>();
    @HostBinding('class.font-opensans') fontFamily = true;

    copyState = {
        bindedContents: false,
        originalContents: false,
        bindValue: false
    };

    constructor(
        private clipboardService: ClipboardService
    ) {}

    ngOnInit() {}
    ngAfterViewChecked() {
        PR.prettyPrint();
    }

    onCopyBindedContents() {
        this.onClickCopy('bindedContents');
        this.clipboardService.copyFromContent(this.formatting(this.data.bindedContents));
    }

    onCopyOriginalContents() {
        this.onClickCopy('originalContents');
        this.clipboardService.copyFromContent(this.formatting(this.data.originalContents));
    }

    onCopyBindValue() {
        this.onClickCopy('bindValue');
        this.clipboardService.copyFromContent(this.data.bindValue);
    }

    onClose() {
        this.outClosePopup.emit();
    }

    hasBind(): boolean {
        return !!this.data.bindValue;
    }

    getClassName(): string {
        return 'lang-' + this.data.type.toLowerCase();
    }

    formatting(code: string): string {
        return this.data.type === 'SQL' ? sqlFormatter.format(code) : js_beautify(code);
    }

    private onClickCopy(key: string): void {
        (this.copyState as any)[key] = true;
        setTimeout(() => {
            (this.copyState as any)[key] = false;
        }, 2000);
    }
}
