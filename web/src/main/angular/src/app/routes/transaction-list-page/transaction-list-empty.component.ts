import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'pp-transaction-list-empty',
    template: `
        <div>
            <span>{{message}}</span>
        </div>
    `,
    styleUrls: ['./transaction-list-empty.component.css'],
})
export class TransactionListEmptyComponent implements OnInit {
    message: string;
    constructor(private translateService: TranslateService) {}
    ngOnInit() {
        this.translateService.get('TRANSACTION_LIST.SELECT_TRANSACTION').subscribe((text: string) => {
            this.message = text;
        });
    }
}
