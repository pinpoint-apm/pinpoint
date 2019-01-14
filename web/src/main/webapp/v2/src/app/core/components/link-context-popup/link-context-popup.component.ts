import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-link-context-popup',
    templateUrl: './link-context-popup.component.html',
    styleUrls: ['./link-context-popup.component.css']
})
export class LinkContextPopupComponent implements OnInit {
    @Output() outClickFilterTransaction = new EventEmitter<void>();
    @Output() outClickFilterTransactionWizard = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}
    onClickFilterTransaction(): void {
        this.outClickFilterTransaction.emit();
    }

    onClickFilterTransactionWizard(): void {
        this.outClickFilterTransactionWizard.emit();
    }
}
