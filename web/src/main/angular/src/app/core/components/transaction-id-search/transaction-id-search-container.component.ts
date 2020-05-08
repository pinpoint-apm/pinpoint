import {
    Component,
    OnInit,
    OnDestroy,
    ChangeDetectionStrategy,
    Output,
    EventEmitter
} from '@angular/core';

import {
    UrlRouteManagerService
} from 'app/shared/services';
import {UrlPath} from "../../../shared/models";

@Component({
    selector: 'pp-transaction-id-search-container',
    templateUrl: './transaction-id-search-container.component.html',
    styleUrls: ['./transaction-id-search-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionIdSearchContainerComponent implements OnInit, OnDestroy {
    @Output() outSearchId: EventEmitter<{txId: string}> = new EventEmitter();

    inputValue: string;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}

    ngOnInit() {}

    ngOnDestroy() {}

    onSearchId(): void {
        const txId = this.inputValue.trim();

        if (txId === '') {
            return;
        }
        const spanId = '-1';
        const collectorAcceptTime = '0';
        const txInfo = txId.split('^');
        if (txInfo.length != 3) {
            return;
        }
        const agentId = txInfo[0];
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_DETAIL,
                txId,
                collectorAcceptTime,
                agentId,
                spanId
            ]
        });
        this.onClear();
    }

    onClear() {
        this.inputValue = '';
    }
}
