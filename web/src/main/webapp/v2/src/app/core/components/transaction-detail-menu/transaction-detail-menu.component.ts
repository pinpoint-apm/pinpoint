import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-transaction-detail-menu',
    templateUrl: './transaction-detail-menu.component.html',
    styleUrls: ['./transaction-detail-menu.component.css']
})

export class TransactionDetailMenuComponent implements OnInit {
    @Input() viewTypeList: object[];
    @Input() viewType: string;
    @Input() partInfo: any;
    @Output() outSelectViewType: EventEmitter<string> = new EventEmitter();
    @Output() outOpenDetailView: EventEmitter<null> = new EventEmitter();
    @Output() outOpenExtraView: EventEmitter<any> = new EventEmitter();
    constructor() {}
    ngOnInit() {}
    isCurrentView(viewType: string): boolean {
        return this.viewType === viewType;
    }
    onSelectView(viewType: string): void {
        if ( this.viewType === viewType ) {
            return;
        }
        this.outSelectViewType.next(viewType);
    }
    openDetailView(): void {
        this.outOpenDetailView.next();
    }
    hasLogView(): boolean {
        return this.partInfo && this.partInfo.logLinkEnable;
    }
    openLogView(): void {
        if  (this.partInfo.loggingTransactionInfo === true ) {
            this.outOpenExtraView.next({
                open: true,
                url: this.partInfo.logPageUrl
            });
        } else {
            this.outOpenExtraView.next({
                open: false,
                message: this.partInfo.disableButtonMessage
            });
        }
    }
    hasInfo(): boolean {
        return this.partInfo && !this.partInfo.loggingTransactionInfo;
    }
    getStateClass(): string {
        if ( this.partInfo ) {
            return 'l-transaction-' + this.partInfo.completeState.toLowerCase();
        } else {
            return '';
        }
    }
    hasState(): boolean {
        return !!this.partInfo;
    }
}
