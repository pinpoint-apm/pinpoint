import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'pp-transaction-list-empty',
    template: `
        <div>
            <span>{{message}}</span>
        </div>
    `,
    styles: [`
        div {
            width: 100%;
            height: 100%;
            display: flex;
            font-size: 20px;
            font-weight: 600;
            align-items: center;
            justify-content: center;
            background-color: rgba(30, 87, 153, 0.3);
        }
    `]
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
