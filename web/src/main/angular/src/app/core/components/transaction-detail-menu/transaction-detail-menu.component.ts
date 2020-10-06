import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-transaction-detail-menu',
    templateUrl: './transaction-detail-menu.component.html',
    styleUrls: ['./transaction-detail-menu.component.css']
})

export class TransactionDetailMenuComponent implements OnInit {
    @Input() activeTabKey: string;
    @Input() transactionDetailInfo: ITransactionDetailData;
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
        }, {
            key: 'timelineV2',
            display: 'Timeline Beta'
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
        return this.transactionDetailInfo && this.transactionDetailInfo.logLinkEnable;
    }

    openLogView(): void {
        this.transactionDetailInfo.loggingTransactionInfo === true
            ? this.outOpenExtraView.next({open: true, url: this.transactionDetailInfo.logPageUrl})
            : this.outOpenExtraView.next({open: false, message: this.transactionDetailInfo.disableButtonMessage});
    }

    hasInfo(): boolean {
        return this.transactionDetailInfo && this.transactionDetailInfo.loggingTransactionInfo;
    }

    getStateClass(): string {
        return this.transactionDetailInfo ? `l-transaction-${this.transactionDetailInfo.completeState.toLowerCase()}` : '';
    }

    hasState(): boolean {
        return !!this.transactionDetailInfo;
    }

    getLogIcon(): string {
        return this.hasInfo() ? 'fas fa-external-link-square-alt' : 'fas fa-ban';
    }
}
