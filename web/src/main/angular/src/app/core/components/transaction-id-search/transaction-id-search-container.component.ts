import {
    Component,
    ChangeDetectionStrategy,
    Output,
    EventEmitter
} from '@angular/core';

import {
    UrlRouteManagerService
} from 'app/shared/services';
import { UrlPath } from 'app/shared/models';

@Component({
    selector: 'pp-transaction-id-search-container',
    templateUrl: './transaction-id-search-container.component.html',
    styleUrls: ['./transaction-id-search-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionIdSearchContainerComponent {
    @Output() outSearchId = new EventEmitter<{txId: string}>();

    searchUseEnter = true;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}

    onSearchId(txId: string): void {
        if (txId === '') {
            return;
        }

        const spanId = '-1';

        const collectorAcceptTime = '0';

        const txInfo = txId.split('^');

        if (txInfo.length !== 3) {
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
    }
}
