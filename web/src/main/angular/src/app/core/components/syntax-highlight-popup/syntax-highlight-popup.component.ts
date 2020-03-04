declare var PR: any;
import { Component, OnInit, Input, Output, EventEmitter, HostBinding, AfterViewChecked } from '@angular/core';
import { ClipboardService } from 'ngx-clipboard';
import { js_beautify } from 'js-beautify';
import sqlFormatter from 'sql-formatter';

@Component({
    selector: 'pp-syntax-highlight-popup',
    templateUrl: './syntax-highlight-popup.component.html',
    styleUrls: ['./syntax-highlight-popup.component.css']
})
export class SyntaxHighlightPopupComponent implements OnInit, AfterViewChecked {
    @Input() data: ISyntaxHighlightData;
    @Output() outClosePopup = new EventEmitter<void>();
    @HostBinding('class.font-opensans') fontFamily = true;

    constructor(private clipboardService: ClipboardService) {}
    ngOnInit() {}
    ngAfterViewChecked() {
        PR.prettyPrint();
    }
    onCopyOriginalContents() {
        this.clipboardService.copyFromContent(this.formatting(this.data.originalContents));
    }
    onCopyBindedContents() {
        this.clipboardService.copyFromContent(this.formatting(this.data.bindedContents));
    }
    onCopyBindValue() {
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
}
