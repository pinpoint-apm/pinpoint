import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';

import { TransactionMetaDataService } from 'app/core/components/transaction-table-grid/transaction-meta-data.service';
@Component({
    selector: 'pp-transaction-list-empty',
    template: `
        <div>
            <span>{{message$ | async}}</span>
        </div>
    `,
    styleUrls: ['./transaction-list-empty.component.css'],
})
export class TransactionListEmptyComponent implements OnInit {
    message$: Observable<string>;

    constructor(
        private translateService: TranslateService,
        private transactionMetaDataService: TransactionMetaDataService,
    ) {}

    ngOnInit() {
        this.message$ = this.transactionMetaDataService.onTransactionDataLoad$.pipe(
            map(({length}: ITransactionMetaData[]) => length === 0),
            filter((isEmpty: boolean) => !isEmpty),
            switchMap(() => this.translateService.get('TRANSACTION_LIST.SELECT_TRANSACTION'))
        );
    }
}
