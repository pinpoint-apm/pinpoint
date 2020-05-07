import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-node-context-popup',
    templateUrl: './node-context-popup.component.html',
    styleUrls: ['./node-context-popup.component.css']
})
export class NodeContextPopupComponent implements OnInit {
    @Output() outClickApplicationFilterTransactionWizard = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}

    onClickApplicationFilterTransactionWizard(): void {
        this.outClickApplicationFilterTransactionWizard.emit();
    }
}
