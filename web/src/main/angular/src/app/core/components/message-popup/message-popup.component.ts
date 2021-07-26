import { Component, OnInit, Input, Output, EventEmitter, HostBinding } from '@angular/core';

@Component({
    selector: 'pp-message-popup',
    templateUrl: './message-popup.component.html',
    styleUrls: ['./message-popup.component.css']
})
export class MessagePopupComponent implements OnInit {
    @Input() data: ITransactionMessage;
    @Output() outClosePopup = new EventEmitter<void>();
    @HostBinding('class.font-opensans') fontFamily = true;

    isPlainMessage: boolean;

    constructor() {}
    ngOnInit() {
        this.isPlainMessage = this.data.type === 'plain';
    }

    onClose(): void {
        this.outClosePopup.emit();
    }
}
