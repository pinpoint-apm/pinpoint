import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-transaction-detail-menu',
    templateUrl: './transaction-detail-menu.component.html',
    styleUrls: ['./transaction-detail-menu.component.css']
})

export class TransactionDetailMenuComponent implements OnInit {
    @Input() activeTabKey: string;
    @Input() partInfo: any;
    @Output() outSelectViewType = new EventEmitter<string>();
    @Output() outOpenDetailView = new EventEmitter<void>();
    @Output() outOpenExtraView = new EventEmitter<any>();

    tabList = [
        {
            key: 'callTree',
            display: 'Call Tree'
        }, {
            key: 'serverMap',
            display: 'Server Map'
        }, {
            key: 'timeline',
            display: 'Timeline'
        }
    ];

    constructor() {}
    ngOnInit() {}
    isActive(key: string): boolean {
        return this.activeTabKey === key;
    }

    onClickTab(key: string): void {
        if (this.activeTabKey === key) {
            return;
        }

        this.outSelectViewType.next(key);
    }

    openDetailView(): void {
        this.outOpenDetailView.next();
    }

    hasLogView(): boolean {
        return this.partInfo && this.partInfo.logLinkEnable;
    }

    openLogView(): void {
        this.partInfo.loggingTransactionInfo === true
            ? this.outOpenExtraView.next({open: true, url: this.partInfo.logPageUrl})
            : this.outOpenExtraView.next({open: false, message: this.partInfo.disableButtonMessage});
    }

    hasInfo(): boolean {
        return this.partInfo && this.partInfo.loggingTransactionInfo;
    }

    getStateClass(): string {
        return this.partInfo ? `l-transaction-${this.partInfo.completeState.toLowerCase()}` : '';
    }

    hasState(): boolean {
        return !!this.partInfo;
    }

    getLogIcon(): string {
        return this.hasInfo() ? 'fas fa-external-link-square-alt' : 'fas fa-ban';
    }
}
